package org.springframework.ide.vscode.boot.properties.completions;

import java.util.Collection;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.boot.common.PropertyCompletionFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.IDocument;;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesCompletionEngine implements ICompletionEngine {

	private boolean preferLowerCaseEnums = true; //might make sense to make this user configurable

	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private PropertyCompletionFactory completionFactory = null;

	/**
	 * Constructor used in 'production'. Wires up stuff properly for running inside a normal
	 * Eclipse runtime.
	 */
	public SpringPropertiesCompletionEngine(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder projectFinder) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.completionFactory = new PropertyCompletionFactory(projectFinder);
	}

	/**
	 * Create completions proposals in the context of a properties text editor.
	 */
	public Collection<ICompletionProposal> getCompletions(IDocument doc, int offset) throws BadLocationException {
		return new PropertiesCompletionProposalsCalculator(indexProvider.getIndex(doc),
				typeUtilProvider.getTypeUtil(doc), completionFactory, doc, offset, preferLowerCaseEnums).calculate();
	}

	public boolean getPreferLowerCaseEnums() {
		return preferLowerCaseEnums;
	}

	public void setPreferLowerCaseEnums(boolean preferLowerCaseEnums) {
		this.preferLowerCaseEnums = preferLowerCaseEnums;
	}

}