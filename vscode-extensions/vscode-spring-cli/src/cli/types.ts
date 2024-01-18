export interface ProjectType {
    id: string;
    description?: string;
    url?: string;
    catalogId: string;
    tags?: string[] 
}

export interface BootNewMetadata {
    name?: string;
    groupId?: string;
    artifactId?: string;
    catalogType?: string;
    rootPackage?: string;
    targetFolder?: string;
}

export interface BootAddMetadata {
    targetFolder?: string;
    catalogType?: string;
}