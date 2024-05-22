export const vectorSearchPrompt = `This is the system chat message. Your task is to create Java source code for an application.
Use the following project information for the solution:
Main Spring project: [Spring Project Name]
Package name: [Package Name]
Build tool: [Build Tool]
Spring boot version: [Spring Boot Version]
Java version: [Java Version]
User prompt: [Description]

Use the information from the CONTENTS section below to provide accurate answers. If unsure or if the answer isn't found in the CONTENTS section, simply state that you don't know the answer. Give elaborate examples with complete code samples by referencing the CONTENTS section. 
CONTENTS: [Contents]
`