import { CancellationToken, DebugConfiguration, DebugConfigurationProvider, ProviderResult, WorkspaceFolder } from "vscode";
import * as path from "path";
import * as VSCode from "vscode";
import { Disposable } from "vscode";
import psList from 'ps-list';
import { ListenablePreferenceSetting } from "@pivotal-tools/commons-vscode/lib/launch-util";

const JMX_VM_ARG = '-Dspring.jmx.enabled='
const ADMIN_VM_ARG = '-Dspring.application.admin.enabled='
const BOOT_PROJECT_ARG = '-Dspring.boot.project.name=';

class SpringBootDebugConfigProvider implements DebugConfigurationProvider {

    resolveDebugConfigurationWithSubstitutedVariables(folder: WorkspaceFolder | undefined, debugConfiguration: DebugConfiguration, token?: CancellationToken): ProviderResult<DebugConfiguration> {
        if (isActuatorOnClasspath(debugConfiguration)) {
            if (debugConfiguration.vmArgs) {
                if (debugConfiguration.vmArgs.indexOf(JMX_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${JMX_VM_ARG}true`;
                }
                if (debugConfiguration.vmArgs.indexOf(ADMIN_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${ADMIN_VM_ARG}true`;
                }
                if (debugConfiguration.vmArgs.indexOf(BOOT_PROJECT_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${BOOT_PROJECT_ARG}${debugConfiguration.projectName}`;
                }
            } else {
                debugConfiguration.vmArgs = `${JMX_VM_ARG}true ${ADMIN_VM_ARG}true ${BOOT_PROJECT_ARG}${debugConfiguration.projectName}`;
            }
        }
        return debugConfiguration;
    }

}

interface ProcessEvent {
    type: string;
    pid: number;
    shellProcessId: number
}

function hookListenerToBooleanPreference(setting: string, listenerCreator: () => Disposable): Disposable {
    const listenableSetting =  new ListenablePreferenceSetting<boolean>(setting);
    let listener: Disposable | undefined = listenableSetting.value ? listenerCreator() : undefined;
    listenableSetting.onDidChangeValue(() => {
        if (listenableSetting.value) {
            if (!listener) {
                listener = listenerCreator();
            }
        } else {
            if (listener) {
                listener.dispose();
                listener = undefined;
            }
        }
    });

    return {
        dispose: () => {
            if (listener) {
                listener.dispose();
            }
            listenableSetting.dispose();
        }
    };
}

export function startDebugSupport(): Disposable {
    return hookListenerToBooleanPreference(
            'boot-java.live-information.automatic-connection.on',
             () => Disposable.from(
                 VSCode.debug.onDidReceiveDebugSessionCustomEvent(handleCustomDebugEvent),
                 VSCode.debug.registerDebugConfigurationProvider('java', new SpringBootDebugConfigProvider(), VSCode.DebugConfigurationProviderTriggerKind.Initial)
             )
    );
}

async function handleCustomDebugEvent(e: VSCode.DebugSessionCustomEvent): Promise<void> {
    if (e.session?.type === 'java' && e?.body?.type === 'processid') {
        const debugConfiguration: DebugConfiguration = e.session.configuration;
        setTimeout(async () => {
            const pid = await getAppPid(e.body as ProcessEvent);
            const processKey = pid.toString();
            VSCode.commands.executeCommand('sts/livedata/connect', { processKey });
        }, 500);
    }
}

async function getAppPid(e: ProcessEvent): Promise<number> {
    if (e.pid) {
        return e.pid;
    } else if (e.shellProcessId) {
        const processes = await psList();
        const appProcess = processes.find(p => p.ppid === e.shellProcessId);
        if (appProcess) {
            return appProcess.pid;
        }
        throw Error(`No child process found for parent shell process with pid = ${e.shellProcessId}`);
    } else {
        throw Error('No pid or parent shell process id available');
    }
}

function isActuatorOnClasspath(debugConfiguration: DebugConfiguration): boolean {
    if (Array.isArray(debugConfiguration.classPaths)) {
        return !!debugConfiguration.classPaths.find(isActuatorJarFile);
    }
    return false;
}

function isActuatorJarFile(f: string): boolean {
    const fileName = path.basename(f || "");
    if (/^spring-boot-actuator-\d+\.\d+\.\d+(.*)?.jar$/.test(fileName)) {
        return true;
    }
    return false;
}
