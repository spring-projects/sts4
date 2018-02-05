import * as FS from 'fs';
import * as Path from 'path';
import * as ChildProcess from 'child_process';

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
    let javaExe = findJavaExe(javaHome);
    if (javaExe) {
        return determineJavaVersion(javaExe).then(version => new JavaExecutable(javaExe, version));
    }
    return Promise.resolve(null);
}

/**
 * Find a 'java' exe by looking in the JAVA_HOME and PATH environment variables.
 * <p>
 * Optionally, a specific javaHome can be passed in. This shortcuts the
 * search logic and uses that javaHome as is, not looking anywhere else.
 */
function findJavaExe(javaHome?: string) : string | null {
    if (!javaHome) {
        javaHome = process.env["JAVA_HOME"];
    }
    if (javaHome) {
        //Resolve symlinks
        javaHome = FS.realpathSync(javaHome);
    }
    let binName = correctBinname("java");
    if (javaHome) {
        return Path.resolve(javaHome, "bin", binName);
    } else {
        for (var searchPath of process.env['PATH'].split(Path.delimiter)) {
            let javaExe = Path.resolve(searchPath, binName);
            if (FS.existsSync(javaExe)) {
                //Resolve symlinks
                return FS.realpathSync(javaExe);
            }
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

class JavaExecutable implements JVM {
    javaExe : string
    version : number
    toolsJar: () => string | null;
    constructor(javaExe : string, version : number) {
        this.javaExe = javaExe;
        this.version = version;
        this.toolsJar = memoize(() => this.findToolsJar());
    }

    getJavaHome() : string {
        return Path.resolve(this.javaExe, "..", "..");
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
}

function determineJavaVersion(javaExecutablePath : string) : Promise<number> {
//Examples of the 'java -version' command output:
// 
// For Java 9:
/*
java version "9.0.4"
Java(TM) SE Runtime Environment (build 9.0.4+11)
Java HotSpot(TM) 64-Bit Server VM (build 9.0.4+11, mixed mode)
*/
// For Java 8:
/*
java version "1.8.0_161"
Java(TM) SE Runtime Environment (build 1.8.0_161-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.161-b12, mixed mode)
*/   
    return new Promise((resolve, reject) => {
        ChildProcess.execFile(javaExecutablePath, ['-version'], {}, (error, stdout, stderr) => {
            let versionStart = stderr.indexOf('"');
            if (versionStart>=0) {
                versionStart = versionStart + 1;
                let versionEnd = stderr.indexOf('"', versionStart);
                if (versionEnd>=0) {
                    let versionString = stderr.substring(versionStart, versionEnd);
                    let pieces = versionString.split(".");
                    let major = parseInt(pieces[0]);
                    major = major==1 ? parseInt(pieces[1]) : major
                    return resolve(major);
                }
            }
            //Unexpected output...
            return resolve(0);
        });
    });

}

function correctBinname(binname: string) {
    if (process.platform === 'win32')
        return binname + '.exe';
    else
        return binname;
}