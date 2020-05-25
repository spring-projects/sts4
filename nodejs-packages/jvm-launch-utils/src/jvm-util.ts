import * as FS from 'fs';
import * as Path from 'path';
import * as ChildProcess from 'child_process';
import { basename } from 'path';

'use strict';

export interface JVM {
    /**
     * 8 = Java 1.8.x, 9 = Java 9.x, etc
     */
    getMajorVersion() : number
    
    /**
     * Path to the Java executable
     */
    getJavaExecutable() : string

    /**
     * Path to the corresponding 'java home' for the executable.
     */
    getJavaHome() : string

    /**
     * Detect whether this JVM is a JDK
     */
    isJdk() : boolean

    /**
     * Find tools.jar for this JVM.
     * 
     * Note that if the JVM is a JRE; or a Java 9 or above JDK;
     * then this will return null.
     */
    getToolsJar() : string | null

    /**
     * Launch an executable jar with this jvm.
     * This autmatically adds tools.jar to the classpath if available.
     * WARNING: For adding tools jar to work properly, the jar must be packaged
     * using spring-boot-maven-plugin ZIP layout.
     */
    jarLaunch(jar: string, vmargs?: string[], execFileOptions?: ChildProcess.ExecFileOptions) : ChildProcess.ChildProcess

    mainClassLaunch(mainClass: string, classpath: string[], jvmArgs: string[], execFileOptions?: ChildProcess.ExecFileOptions): ChildProcess.ChildProcess
}

/**
 * Find a JVM by looking in the JAVA_HOME and PATH environment variables.
 * 
 * Optionally, a specific javaHome can be passed in. This shortcuts the
 * search logic and uses that javaHome as is.
 * 
 * The returned JVM may or may not be a JDK. Methods are provided to obtain corresponding
 * toolsjar and to check whether the JVM is a JDK.
 */
export function findJvm(javaHome?: string) : Promise<JVM | null> {
    try {
        let javaExe = findJavaExe(javaHome);
        if (javaExe) {
            return getJavaInfo(javaExe).then(javaProps => new JVMImpl(
                javaProps.get("java.home"), 
                javaExe, 
                getMajorVersion(javaProps)
            ));
        }
        return Promise.resolve(null);
    } catch (e) {
        return Promise.reject(e);
    }
}

/**
 * Like findJvm, but additionally, if the found JVM is not a JDK tries to
 * find a companion JDK that may be installed alongside it. 
 */
export function findJdk(javaHome?: string) : Promise<JVM | null> {
    return findJvm(javaHome).then(jvm => {
        if(!jvm) {
            return null;
        }
        if (!jvm.isJdk()) {
            console.log("found jvm is not a JDK");

            //Try to find a 'sibling' JDK. 
            //Mainly for windows where it is common to have side-by-side install of a jre and jdk, instead of a
            //nested jre install inside of a jdk.
            
            //E.g.
            //C:\ProgramFiles\Java\jdk1.8.0_161
            //C:\ProgramFiles\Java\jre1.8.0_161

            let javaExe = jvm.getJavaExecutable();
            console.log("javaExe = ", javaExe);
            // javaExe example: C:\ProgramFiles\Java\jre1.8.0_161\bin\java.exe
            let jhome = jvm.getJavaHome();
            console.log("jhome = ", jhome);
            let basename : string = Path.basename(jhome);
            console.log("basename = ", basename);
            let altBasename : string = basename.replace("jre", "jdk");
            console.log("altBasename = ", altBasename);
            if (altBasename!==basename) {
                let altHome = Path.join(Path.dirname(jhome), altBasename);
                console.log("altHome = ", altHome);
                if (FS.existsSync(altHome)) {
                    let altExe = Path.resolve(altHome, "bin", correctBinname("java"));
                    console.log("altExe = ", altExe);
                    return new JVMImpl(altHome, altExe, jvm.getMajorVersion());
                }
            }
        }
        return jvm;
    });
}

/**
 * Find a 'java' exe by looking in the JAVA_HOME and PATH environment variables.
 * <p>
 * Optionally, a specific javaHome can be passed in. This shortcuts the
 * search logic and uses that javaHome as is, not looking anywhere else.
 */
function findJavaExe(javaHome?: string) : string | null {
    //Try java home first
    if (!javaHome) {
        javaHome = process.env["JAVA_HOME"];
    }
    if (javaHome) {
        //Resolve symlinks
        javaHome = FS.realpathSync(javaHome);
    }
    let binName = correctBinname("java");
    if (javaHome) {
        let javaExe = Path.resolve(javaHome, "bin", binName);
        if (FS.existsSync(javaExe)) {
            return javaExe;
        }
    }

    for (var searchPath of process.env['PATH'].split(Path.delimiter)) {
        let javaExe = Path.resolve(searchPath, binName);
        if (FS.existsSync(javaExe)) {
            //Resolve symlinks
            return FS.realpathSync(javaExe);
        }
    }
    return null;
}

type Getter<T> = () => T;

function memoize<T>(getter : Getter<T>) : Getter<T> {
    let computed : boolean = false;
    let value : T | null = null;
    return () => {
        if (!computed) {
            value = getter();
            computed = true;
        }
        return value;
    };
}

const TOOLS_JAR_PATHS : string[][] = [
    ["lib", "tools.jar"],
    ["..", "lib", "tools.jar"]
];

class JVMImpl implements JVM {
    javaHome : string
    javaExe : string
    version : number
    toolsJar: () => string | null;
    constructor(javaHome : string, javaExe : string, version : number) {
        this.javaHome = javaHome;
        this.javaExe = javaExe;
        this.version = version;
        this.toolsJar = memoize(() => this.findToolsJar());
    }

    getJavaHome() : string {
        return this.javaHome;
    }

    findToolsJar() : string | null {
        if (this.version>=9) {
            return null;
        }
        let javaHome = this.getJavaHome();
        for (var tjp of TOOLS_JAR_PATHS) {
            let toolsJar = Path.resolve(javaHome, ...tjp);
            if (FS.existsSync(toolsJar)) {
                return toolsJar;
            }
        }
        //Not found.
        return null;
    }

    getMajorVersion() {
        return this.version;
    }
    getJavaExecutable(): string {
        return this.javaExe;
    }
    isJdk(): boolean {
        //Consider memoizing?
        if (this.version<9) {
            return this.getToolsJar()!=null;
        } else {
            return FS.existsSync(Path.resolve(this.getJavaHome(), "jmods", "jdk.management.jmod"));
        }
    }
    getToolsJar(): string {
        return this.toolsJar();
    }
    jarLaunch(jar: string, vmargs?: [string], execFileOptions?: ChildProcess.ExecFileOptions): ChildProcess.ChildProcess {
        let args = [];
        let toolsJar = this.getToolsJar();
        if (toolsJar) {
            args.push("-Dloader.path="+toolsJar);
        }
        if (vmargs) {
            args.push(...vmargs);
        }
        args.push("-jar", jar);
        return ChildProcess.execFile(this.getJavaExecutable(), args, execFileOptions);
    }

    mainClassLaunch(mainClass: string, classpath: string[], jvmArgs: string[], execFileOptions?: ChildProcess.ExecFileOptions): ChildProcess.ChildProcess {
        const args: string[] = [];

        // Classpath
        args.push('-cp');
        let classpathStr = classpath.join(Path.delimiter);
        const toolsJar = this.getToolsJar();
        if (toolsJar) {
            classpathStr += Path.delimiter + toolsJar;
        }
        args.push(classpathStr);

        // JVM Arguments
        args.push(...jvmArgs);

        // Main class
        args.push(mainClass);

        return ChildProcess.execFile(this.getJavaExecutable(), args, execFileOptions);
    }
}

function getJavaInfo(javaExe : string) : Promise<Map<string, string>> {
    //console.log("Fetching java properties for "+javaExe);
    return new Promise((resolve, reject) => {
        ChildProcess.execFile(javaExe, ['-XshowSettings:properties'], {}, (error, stdout, stderr) => {
            let lines = stderr.split(/\r?\n/);
            let propNames = [ 'java.version', 'java.home' ];
            let props = new Map<string,string>();
            for (var l of lines) {
                //console.log("Line: "+l);
                for (var p of propNames) {
                    let offset = l.indexOf(p);
                    if (offset>=0) {
                        //Make sure it looks like a proper 'assignment' to the property and not an
                        // accidental match.
                        //console.log("Propname found: "+p);
                        let assign = " " +p + " = ";
                        offset = l.indexOf(assign);
                        if (offset>=0) {
                            //console.log("Assignment found: "+p);
                            offset = offset + assign.length;
                            let value = l.substring(offset);
                            //console.log("value = "+value);
                            props.set(p, value);
                            if (props.size >= propNames.length) {
                                //We found everything we care about, so we can stop now.
                                //console.log("result = ", props);
                                return resolve(props);
                            }
                        }
                    }
                }
            }
            //Not found everything we expected.
            return reject("Unexpected output from `java -XshowSettings:properties`. Didn't find all expected properties: "+propNames);
        });
    });
}

function getMajorVersion(javaProperties : Map<string,string>) : number {
    let versionString = javaProperties.get('java.version');
    let pieces = versionString.split(".");
    let major = parseInt(pieces[0]);
    return major==1 ? parseInt(pieces[1]) : major;
}

function correctBinname(binname: string) {
    if (process.platform === 'win32')
        return binname + '.exe';
    else
        return binname;
}