export interface CliProjectType {
    id: string;
    description?: string;
    url?: string;
    catalogId: string;
    tags?: string[] 
}

export interface CliNewProjectMetadata {
    name?: string;
    catalogType?: string;
    rootPackage?: string;
    targetFolder?: string;
}