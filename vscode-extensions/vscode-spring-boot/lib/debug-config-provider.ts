import { debug,
    window,
    commands,
    workspace,
    CancellationToken,
    DebugConfiguration,
    DebugConfigurationProvider,
    WorkspaceFolder,
    DebugConfigurationProviderTriggerKind,
    DebugSession,
    DebugSessionCustomEvent,
    Disposable
} from "vscode";
import * as path from "path";
import psList from 'ps-list';
import * as fs from "fs";
import { tmpdir } from "os";
import { randomUUID } from "crypto";

const JMX_VM_ARG = '-Dspring.jmx.enabled='
const ACTUATOR_JMX_EXPOSURE_ARG = '-Dmanagement.endpoints.jmx.exposure.include='
const ADMIN_VM_ARG = '-Dspring.application.admin.enabled='
const BOOT_PROJECT_ARG = '-Dspring.boot.project.name=';
const RMI_HOSTNAME = '-Djava.rmi.server.hostname=localhost';

const ENV_TESTJAR_ARTIFACT_PREFIX = "TESTJARS_ARTIFACT_";

const TEST_RUNNER_MAIN_CLASSES = [
    'org.eclipse.jdt.internal.junit.runner.RemoteTestRunner',
    'com.microsoft.java.test.runner.Launcher'
];

interface ExecutableBootProject {
    name: string;
    uri: string;
    mainClass: string;
    classpath: string[];
    gav: string;
}

interface ProcessEvent {
    type: string;
    processId: number;
    shellProcessId: number
}

class SpringBootDebugConfigProvider implements DebugConfigurationProvider {

    async resolveDebugConfigurationWithSubstitutedVariables(folder: WorkspaceFolder | undefined, debugConfiguration: DebugConfiguration, token?: CancellationToken): Promise<DebugConfiguration> {
        // TestJar launch support
        if (TEST_RUNNER_MAIN_CLASSES.includes(debugConfiguration.mainClass) && isTestJarsOnClasspath(debugConfiguration)) {
            const projects = await commands.executeCommand("sts/spring-boot/executableBootProjects") as ExecutableBootProject[];
            let env = debugConfiguration.env;
            if (!env) {
                env = {};
                debugConfiguration.env = env;
            }
            const projectsWithErrors: ExecutableBootProject[] = [];
            // Create all project classparth data files and add env vars for workspace projects
            await Promise.all(projects.map(async p => {
                const envName = this.createEnvVarName(p);
                if (!env[envName]) {
                    try {
                        env[envName] = await this.createFile(p);
                    } catch (error) {
                        projectsWithErrors.push(p);
                    }
                }
            }));
            if (projectsWithErrors.length > 0) {
                const projectStr = projectsWithErrors.map(p => `'${p.name}'`);
                window.showWarningMessage(`TestJar Support: Could not provide data for workspace projects: ${projectStr}`);
            }
        }
        // Running app live hovers support
        if (isAutoConnectOn() && !TEST_RUNNER_MAIN_CLASSES.includes(debugConfiguration.mainClass) && isActuatorOnClasspath(debugConfiguration)) {
            if (debugConfiguration.vmArgs) {
                if (debugConfiguration.vmArgs.indexOf(JMX_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${JMX_VM_ARG}true`;
                }
                if (debugConfiguration.vmArgs.indexOf(ACTUATOR_JMX_EXPOSURE_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${ACTUATOR_JMX_EXPOSURE_ARG}*`;
                }
                if (debugConfiguration.vmArgs.indexOf(ADMIN_VM_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${ADMIN_VM_ARG}true`;
                }
                if (debugConfiguration.vmArgs.indexOf(BOOT_PROJECT_ARG) < 0) {
                    debugConfiguration.vmArgs += ` ${BOOT_PROJECT_ARG}${debugConfiguration.projectName}`;
                }
                if (debugConfiguration.vmArgs.indexOf(RMI_HOSTNAME) < 0) {
                    debugConfiguration.vmArgs += ` ${RMI_HOSTNAME}`;
                }
            } else {
                debugConfiguration.vmArgs = `${JMX_VM_ARG}true ${ACTUATOR_JMX_EXPOSURE_ARG}* ${ADMIN_VM_ARG}true ${BOOT_PROJECT_ARG}${debugConfiguration.projectName} ${RMI_HOSTNAME}`;
            }
        }
        return debugConfiguration;
    }

    private createEnvVarName(project: ExecutableBootProject) {
        return `${ENV_TESTJAR_ARTIFACT_PREFIX}${project.gav.replace(/:/g, "_")}`;
    }

    private async createFile(project: ExecutableBootProject) {
        const filePath = path.join(tmpdir(), `${project.gav.replace(/:/g, "_")}-${randomUUID()}`);
        await fs.writeFile(filePath, `# the main class to invoke\nmain=${project.mainClass}\n# the classpath to use delimited by the OS specific delimiters\nclasspath=${project.classpath.join(path.delimiter)}`, function(err) {
            if(err) {
                throw Error();
            }
        }); 
        return filePath;
    }

}

export function startDebugSupport(): Disposable {
    return Disposable.from(
        debug.onDidReceiveDebugSessionCustomEvent(handleCustomDebugEvent),
        debug.onDidTerminateDebugSession(cleanupDebugSession),
        debug.registerDebugConfigurationProvider('java', new SpringBootDebugConfigProvider(), DebugConfigurationProviderTriggerKind.Initial),
        new Disposable(() => cleanupDebugSession(debug.activeDebugSession)) // If VSCode is shutdown then clean active debug session if it satidfies conditions
    );
}

async function cleanupDebugSession(session: DebugSession) {
    // Handle termination of a Boot app with TestJars on the classpath
    if (session.type === 'java' && TEST_RUNNER_MAIN_CLASSES.includes(session.configuration.mainClass) && isTestJarsOnClasspath(session.configuration) && session.configuration.env) {
        await Promise.all(Object.keys(session.configuration.env).filter(k => k.startsWith(ENV_TESTJAR_ARTIFACT_PREFIX)).map(k => fs.rm(session.configuration.env[k], () => {})));
    }
}

async function handleCustomDebugEvent(e: DebugSessionCustomEvent): Promise<void> {
    if (isAutoConnectOn() && e.session?.type === 'java' && e?.body?.type === 'processid') {
        const debugConfiguration: DebugConfiguration = e.session.configuration;
        if (canConnect(debugConfiguration)) {
            setTimeout(async () => {
                const pid = await getAppPid(e.body as ProcessEvent);
                const processKey = pid.toString();
                commands.executeCommand('sts/livedata/connect', { processKey });
            }, 500);
        }
    }
}

async function getAppPid(e: ProcessEvent): Promise<number> {
    if (e.processId && e.processId > 0) {
        return e.processId;
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

function isTestJarsOnClasspath(debugConfiguration: DebugConfiguration): boolean {
    if (Array.isArray(debugConfiguration.classPaths)) {
        return !!debugConfiguration.classPaths.find(isTestJarFile);
    }
    return false;

}

function isTestJarFile(f: string): boolean {
    const fileName = path.basename(f || "");
    if (/^spring-boot-testjars-\d+\.\d+\.\d+(.*)?.jar$/.test(fileName)) {
        return true;
    }
    return false;
}

function isAutoConnectOn(): boolean {
    return workspace.getConfiguration().get("boot-java.live-information.automatic-connection.on", true);
}

function canConnect(debugConfiguration: DebugConfiguration): boolean {
    if (!TEST_RUNNER_MAIN_CLASSES.includes(debugConfiguration.mainClass) && isActuatorOnClasspath(debugConfiguration)) {
        return debugConfiguration.vmArgs
            && debugConfiguration.vmArgs.indexOf(`${JMX_VM_ARG}true`) >= 0
            && debugConfiguration.vmArgs.indexOf(`${ADMIN_VM_ARG}true`) >= 0
    }
    return false;
}
