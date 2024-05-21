package org.springframework.ide.vscode.boot.app.ai;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {
	
	private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

	@Value("${app.vectorstore.path:/tmp/vectorstore.json}")
	private String vectorStorePath;

//    @Bean
//    SimpleVectorStore simpleVectorStore(EmbeddingClient embeddingClient) {
//        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingClient);
//        File vectorStoreFile = new File(vectorStorePath);
//        if (vectorStoreFile.exists()) { // load existing vector store if exists
//        	System.out.println("File exists");
//            simpleVectorStore.load(vectorStoreFile);
//        } else { // otherwise load the documents and save the vector store
//        	System.out.println("Read file from resources");
//            TikaDocumentReader documentReader = new TikaDocumentReader("classpath:/Spring_in_Action_Sixth_Edition.pdf");
//            List<Document> documents = documentReader.get();
//            TextSplitter textSplitter = new TokenTextSplitter();
//            List<Document> splitDocuments = textSplitter.apply(documents);
//            simpleVectorStore.add(splitDocuments);
//            simpleVectorStore.save(vectorStoreFile);
//        }
//        return simpleVectorStore;
//    }

	@Bean
	VectorStore vectorStore(EmbeddingClient embeddingClient) {
		log.info("creating the vector store bean");
		SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingClient);
		File vectorStoreFile = new File(vectorStorePath);
		if (vectorStoreFile.exists()) { // load existing vector store if exists
			System.out.println("File exists");
			simpleVectorStore.load(vectorStoreFile);
		} else { // otherwise load the documents and save the vector store
			log.info("Read file from resources");
//            TikaDocumentReader documentReader = new TikaDocumentReader("classpath:/Spring_in_Action_Sixth_Edition.pdf");
			TikaDocumentReader documentReader = new TikaDocumentReader("classpath:/spring-boot-3-jpa-example.txt");
			List<Document> documents = documentReader.get();
			TextSplitter textSplitter = new TokenTextSplitter();
			List<Document> splitDocuments = textSplitter.apply(documents);
			simpleVectorStore.add(splitDocuments);
			simpleVectorStore.save(vectorStoreFile);
		}
		return simpleVectorStore;
	}
}