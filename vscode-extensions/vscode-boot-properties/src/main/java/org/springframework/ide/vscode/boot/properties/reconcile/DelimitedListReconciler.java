package org.springframework.ide.vscode.boot.properties.reconcile;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;

/**
 * Helper class to reconcile text contained in a document region as a comma-separated list.
 *
 * @author Kris De Volder
 */
public class DelimitedListReconciler {

	interface TypeBasedReconciler {
		void reconcile(DocumentRegion region, Type expectType, IProblemCollector problems);
	}

	private final TypeBasedReconciler valueReconciler;
	private final Pattern delimiter;

	public DelimitedListReconciler(Pattern delimiter, TypeBasedReconciler valueReconciler) {
		this.valueReconciler = valueReconciler;
		this.delimiter = delimiter;
	}

	public void reconcile(DocumentRegion region, Type listType, IProblemCollector problems) {
		Type elType = getElementType(listType);
		//Its pointless to reconcile list of we can't determine value type.
		if (elType!=null) {
			Arrays.stream(region.split(delimiter)).forEach(entry -> {
				valueReconciler.reconcile(entry, elType, problems);
			});
		}
	}

	private Type getElementType(Type listType) {
		Type elType = TypeUtil.getDomainType(listType);
		if (elType!=null) {
			Type nestedElType = getElementType(elType);
			if (nestedElType!=null) {
				return nestedElType;
			}
			return elType;
		}
		return null;
	}

}
