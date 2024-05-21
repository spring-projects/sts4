package org.springframework.ide.vscode.boot.app.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VectorStoreHandler {

	private static final Logger log = LoggerFactory.getLogger(VectorStoreHandler.class);

	private static final String CMD_COPILOT_SEARCH = "sts/copilot/search";

	private static final String CMD_COPILOT_TEST = "sts/copilot/test";

	private static final String PROMPT = """
			You are a helpful assistant, conversing with a user about the subjects contained in a set of documents.
			Use the information from the DOCUMENTS section to provide accurate answers. If unsure or if the answer
			isn't found in the DOCUMENTS section, simply state that you don't know the answer.
			QUESTION:
			{input}
			DOCUMENTS:
			{documents}""";

	final private SimpleLanguageServer server;
	private VectorStore vectorStore;
	private ChatClient aiClient;

	public VectorStoreHandler(SimpleLanguageServer server, VectorStore vectorStore, ChatClient aiClient) {
		this.server = server;
		this.vectorStore = vectorStore;
		this.aiClient = aiClient;
		registerCommands();
	}

	private void registerCommands() {
		server.onCommand(CMD_COPILOT_SEARCH, (params) -> {
			return getContext(params);
		});
		log.info("Registered command handler: {}", CMD_COPILOT_SEARCH);

		server.onCommand(CMD_COPILOT_TEST, params -> {
			return CompletableFuture.completedFuture("executed");
		});
	}

	private CompletableFuture<Object> getContext(ExecuteCommandParams params) {
		log.info(params.toString());
		log.info("Question: " + getArgumentByKey(params, "question"));
		String question = getArgumentByKey(params, "question");
		List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(question).withTopK(2));
		List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();

		log.info("Similar documents: " + contentList);

		PromptTemplate promptTemplate = new PromptTemplate(PROMPT);
		Map<String, Object> promptParameters = new HashMap<>();
		promptParameters.put("input", question);
		promptParameters.put("documents", String.join("\n", contentList));
		Prompt prompt = promptTemplate.create(promptParameters);
		log.info("Prompt: " + prompt);
		return CompletableFuture.completedFuture(contentList);
//	        ChatResponse response = aiClient.call(prompt);
//	        List<Generation> resp = response.getResults();
//	        String resp = aiClient.call(prompt).getResult().getOutput().getContent();
//	        return CompletableFuture.completedFuture(resp);
	}

	private String getArgumentByKey(ExecuteCommandParams params, String name) {
		List<Object> arguments = params.getArguments();
		for (Object arg : arguments) {
			if (arg instanceof Map<?, ?>) {
				Object value = ((Map<?, ?>) arg).get(name);
				if (value != null) {
					return value.toString();
				}
			} else if (arg instanceof JsonObject) {
				JsonElement element = ((JsonObject) arg).get(name);
				if (element != null && element instanceof JsonObject) {
					return element.toString();
				} else if (element != null) {
					return element.getAsString();
				}
			}
		}

		return null;
	}

}