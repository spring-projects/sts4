import cp from 'child_process';
import { CliNewProjectMetadata, CliProjectType } from "./types";

export class Cli {

    getProjectTypes() : CliProjectType[] {
        return [
            {
                id: 'web',
                description: 'Hello, World RESTful web service.',
                url: 'https://github.com/rd-1-2022/rest-service',
                catalogId: 'gs',
                tags: ['java-17', 'boot-3.1.x', 'rest', 'web']
            },
            {
                id: 'jpa',
                description: 'Learn how to work with JPA data persistence using Spring Data JPA.',
                url: 'https://github.com/rd-1-2022/rpt-spring-data-jpa',
                catalogId: 'gs',
                tags: ['java-17', 'boot-3.1.x', 'jpa', 'h2']
            },
            {
                id: 'scheduling',
                description: 'How to schedule tasks',
                url: 'https://github.com/rd-1-2022/rpt-spring-scheduling-tasks',
                catalogId: 'gs',
                tags: ['scheduling']
            }
        ];
    }

    createProject(metadata: CliNewProjectMetadata) {
        this.exec(metadata.targetFolder, `spring boot new ${metadata.name} ${metadata.catalogType}`)
    }

    private exec(cwd: string, cmd: string) {
        cp.execSync(cmd, {
            cwd
        });
    }
    
}

