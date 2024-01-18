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
    catalogType?: string;
}

export interface ProjectCatalog {
    name: string;
    url: string;
    description?: string;
    tags?: string[];
}