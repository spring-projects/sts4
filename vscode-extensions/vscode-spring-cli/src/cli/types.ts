export interface ProjectType {
    id: string;
    description?: string;
    url?: string;
    catalogId: string;
    tags?: string[] 
}

export interface BootNewProjectMetadata {
    name?: string;
    groupId?: string;
    artifactId?: string;
    catalogType?: string;
    rootPackage?: string;
    targetFolder?: string;
}