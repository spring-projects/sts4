package org.springframework.ide.vscode.boot.java.handlers;

public enum QueryType {
    SPEL("Explain SpEL Expression using Copilot", "Explain the following SpEL Expression with a clear summary first, followed by a breakdown of the expression with details: \n\n"),
    JPQL("Explain Query using Copilot", "Explain the following JPQL query with a clear summary first, followed by a detailed explanation. If the query contains any SpEL expressions, explain those parts as well: \n\n"),
    HQL("Explain Query using Copilot", "Explain the following HQL query with a clear summary first, followed by a detailed explanation. If the query contains any SpEL expressions, explain those parts as well: \n\n"),
    AOP("Explain AOP using Copilot", "Explain the following AOP annotation with a clear summary first, followed by a detailed contextual explanation of its usage and any parameters it includes: \n\n"),
    DEFAULT("Explain Query using Copilot", "Explain the following query with a clear summary first, followed by a detailed explanation: \n\n");

    private final String title;
    private final String prompt;

    QueryType(String title, String prompt) {
        this.title = title;
        this.prompt = prompt;
    }

    public String getTitle() {
        return title;
    }

    public String getPrompt() {
        return prompt;
    }
}
