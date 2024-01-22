export interface Project {
    name: string;
    url: string;
    description?: string;
    catalog?: string;
    tags?: string[] 
}

export interface BootNewMetadata {
    name?: string;
    groupId?: string;
    artifactId?: string;
    catalogId?: string;
    rootPackage?: string;
    targetFolder?: string;
}

export interface BootAddMetadata {
    targetFolder?: string;
    catalog?: string;
}

export interface CommandAddMetadata {
    url: string;
}

export interface CommandRemoveMetadata {
    command: string;
    subcommand: string;
    cwd: string;
}

export interface ProjectCatalog {
    name: string;
    url: string;
    description?: string;
    tags?: string[];
}

export interface CommandInfo {
    description?: string
    options: CommandOptions[];
}

export interface CommandOptions {
    name: string;
    description?: string;
    paramLabel?: string;
    dataType: string;
    defaultValue: string;
    inputType: string;
    required: boolean;
    choices: {[name: string]: string}
}

export interface CommandExecuteMetadata {
    command: string;
    subcommand: string;
    params: {[name: string]: string};
}