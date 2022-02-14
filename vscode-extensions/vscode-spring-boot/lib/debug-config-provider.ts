import { CancellationToken, DebugConfiguration, DebugConfigurationProvider, ProviderResult, WorkspaceFolder } from "vscode";
import * as path from "path";
import * as VSCode from "vscode";
import { Disposable } from "vscode";

const JMX_VM_ARG = '-Dspring.jmx.enabled='
const ADMIN_VM_ARG = '-Dspring.application.admin.enabled='
const BOOT_PROJECT_ARG = '-Dspring.boot.project.name=';

class SpringBootDebugConfigProvider implements DebugConfigurationProvider {

    resolveDebugConfigurationWithSubstitutedVariables(folder: WorkspaceFolder | undefined, debugConfiguration: DebugConfiguration, token?: CancellationToken): ProviderResult<DebugConfiguration> {
        if (isAutoConnect() && this.isActuatorOnClasspath(debugConfiguration)) {
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
    // VSCode.debug.onDidStartDebugSession(handleDebugSessionStarted);
    return VSCode.debug.registerDebugConfigurationProvider('java', new SpringBootDebugConfigProvider(), VSCode.DebugConfigurationProviderTriggerKind.Initial);
}

function isAutoConnect(): boolean {
    return VSCode.workspace.getConfiguration("boot-java.live-information.automatic-tracking")?.get('on');
}
