import { CancellationToken, DebugConfiguration, DebugConfigurationProvider, ProviderResult, WorkspaceFolder } from "vscode";
import * as path from "path";
import * as VSCode from "vscode";
import { Disposable } from "vscode";
import { ProcessCommandInfo } from "./live-hover-connect-ui";

const JMX_VM_ARG = '-Dspring.jmx.enabled=true'
const ADMIN_VM_ARG = '-Dspring.application.admin.enabled=true'

class SpringBootDebugConfigProvider implements DebugConfigurationProvider {

    resolveDebugConfigurationWithSubstitutedVariables(folder: WorkspaceFolder | undefined, debugConfiguration: DebugConfiguration, token?: CancellationToken): ProviderResult<DebugConfiguration> {
        if (isAutoConnect() && this.isActuatorOnClasspath(debugConfiguration)) {
            if (debugConfiguration.vmArgs) {
                if (debugConfiguration.vmArgs.indexOf(JMX_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${JMX_VM_ARG}`;
                }
                if (debugConfiguration.vmArgs.indexOf(ADMIN_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${ADMIN_VM_ARG}`;
                }
            } else {
                debugConfiguration.vmArgs = `${JMX_VM_ARG} ${ADMIN_VM_ARG}`;
            }
        }
        return debugConfiguration;
    }

    private isActuatorOnClasspath(debugConfiguration: DebugConfiguration): boolean {
        if (Array.isArray(debugConfiguration.classPaths)) {
            return !!debugConfiguration.classPaths.find(this.isActuatorJarFile);
        }
        return false;
    }

    private isActuatorJarFile(f: string): boolean {
        const fileName = path.basename(f || "");
        if (/^spring-boot-actuator-\d+\.\d+\.\d+(.*)?.jar$/.test(fileName)) {
            return true;
        }
        return false;
    }

}

export function startDebugSupport(): Disposable {
    VSCode.debug.onDidStartDebugSession(handleDebugSessionStarted);
    return VSCode.debug.registerDebugConfigurationProvider('java', new SpringBootDebugConfigProvider(), VSCode.DebugConfigurationProviderTriggerKind.Initial);
}

function isAutoConnect(): boolean {
    return VSCode.workspace.getConfiguration("boot-java.live-information.automatic-tracking")?.get('on');
}

function handleDebugSessionStarted(e: VSCode.DebugSession): void {
    if (e.configuration.type === 'java'
        && isAutoConnect()
        && e.configuration.vmArgs
        && e.configuration.vmArgs.indexOf(JMX_VM_ARG) >= 0
        && e.configuration.vmArgs.indexOf(ADMIN_VM_ARG) >= 0) {
        attemptToConnect(e.configuration.mainClass, 5, 3000);
    }
}

async function attemptToConnect(mainClass: string, attempts: number, interval: number) {
    try {
        const info = await attemptToFindProcess(mainClass, attempts, interval);
        console.log(`Connecting to ${info.processKey}`)
        await VSCode.commands.executeCommand(info.action, info);
    } catch (e) {
        VSCode.window.showErrorMessage(`Failed to automatically connect to Boot app process corresponding to ${mainClass}`);
    }
}

function attemptToFindProcess(mainClass: string, attempts: number, interval: number): Promise<ProcessCommandInfo> {
    return new Promise((resolve, reject) => {
        const intervalHandle = setInterval(async () => {
            try {
                console.log(`Looking for launched java process corresponding to Boot app ${mainClass}. Attempts left ${attempts}.`);
                const processData: ProcessCommandInfo[] = await VSCode.commands.executeCommand('sts/livedata/listProcesses');
                const info = processData.find(pd => pd.processKey.endsWith(mainClass) && pd.action === 'sts/livedata/connect');
                if (info) {
                    clearInterval(intervalHandle);
                    resolve(info);
                }
            } catch (e) {
                // ignore
            }
            attempts--;
            if (attempts === 0) {
                clearInterval(intervalHandle);
                // TODO: show error message
                const msg = `Cannot find Boot application process corresponding to launched ${mainClass}`;
                reject();
            }
        }, interval);
    });
}
