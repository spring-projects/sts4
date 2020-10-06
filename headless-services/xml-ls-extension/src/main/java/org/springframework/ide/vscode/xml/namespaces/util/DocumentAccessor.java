package org.springframework.ide.vscode.xml.namespaces.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Utility that manages an internal stack of {@link Document}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class DocumentAccessor {

	private Stack<Document> documents = new Stack<Document>();
	
	private Stack<Node> elements = new Stack<Node>();
	
	private Node lastElement = null;
	
	private Document lastDocument = null;
	
	private Map<Document, SchemaLocations> schemaLocations = new HashMap<Document, SchemaLocations>(); 

	/** Push a new document onto the internal stack structure */
	public void pushDocument(Document doc) {
		lastDocument = doc;
		documents.push(doc);
	}
	
	/** Push a new element onto the internal stack structure */
	public void pushElement(Node element) {
		lastElement = element;
		elements.push(element);
	}

	/** Returns the current document; meaning the first document in the stack */
	public Document getCurrentDocument() {
		if (!documents.isEmpty()) {
			return documents.peek();
		}
		return null;
	}
	
	/** Retuns the last procssed document */
	public Document getLastDocument() {
		return lastDocument;
	}
	
	/** Returns the current element; meaning the first element in the stack */
	public Node getCurrentElement() {
		if (!elements.isEmpty()) {
			return elements.peek();
		}
		return null;
	}
	
	/** Returns the last processed element */
	public Node getLastElement() {
		return lastElement;
	}
	
	/** Returns the current values of the schemaLocation attribute */
	public synchronized SchemaLocations getCurrentSchemaLocations() {
		Document doc = getCurrentDocument();
		if (!this.schemaLocations.containsKey(doc)) {
			if (doc != null && doc.getDocumentElement() != null) {
				SchemaLocations schemaLocations = new SchemaLocations();
				schemaLocations.initSchemaLocations(doc.getDocumentElement().getAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
				this.schemaLocations.put(doc, schemaLocations);
			}
		}
		return this.schemaLocations.get(doc);
	}

	/** Removes the first document from the stack */
	public Document popDocument() {
		if (!documents.isEmpty()) {
			return documents.pop();
		}
		return null;
	}
	
	/** Removes the first element from the stack */
	public Node popElement() {
		if (!elements.isEmpty()) {
			return elements.pop();
		}
		return null;
	}
	
	/**
	 * Internal class that parses the value of the <code>schemaLocation</code> attribute and offers accessors to the
	 * mapping.
	 */
	public static class SchemaLocations {

		private Map<String, String> mapping = new HashMap<String, String>();

		public void initSchemaLocations(String schemaLocations) {
			if (schemaLocations != null && !schemaLocations.isEmpty()) {
				String[] tokens = schemaLocations.split("\\s*\\r?\\n\\s*");
				if (tokens.length % 2 == 0) {
					for (int i = 0; i < tokens.length; i = i + 2) {
						mapping.put(tokens[i], tokens[i + 1]);
					}
				}
			}
		}

		public String getSchemaLocation(String namespaceUri) {
			return mapping.get(namespaceUri);
		}
	}

}
