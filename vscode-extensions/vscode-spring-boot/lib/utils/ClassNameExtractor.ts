export class ClassNameExtractor {
    
    private patternStrings: string[] = [
        "(?<=\\bclass\\s)\\w+",
        "(?<=\\binterface\\s)\\w+",
        "(?<=\\b@interface\\s)\\w+",
        "(?<=\\benum\\s)\\w+"
    ];
    private patterns: RegExp[] = [];

    constructor() {
        for (let patternString of this.patternStrings) {
            this.patterns.push(new RegExp(patternString, 'g'));
        }
    }

    extractFileNames(codeBlock: string): string {
        let names: string[] = [];
        for (let pattern of this.patterns) {
            let match;
            if ((match = pattern.exec(codeBlock)) !== null) {
                return match;
            }
        }
    }
}