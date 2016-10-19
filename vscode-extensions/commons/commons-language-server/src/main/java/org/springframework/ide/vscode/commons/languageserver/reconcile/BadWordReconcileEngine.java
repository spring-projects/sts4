package org.springframework.ide.vscode.commons.languageserver.reconcile;

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

/**
 * A fake reconcule engine which is not useful except for quickly testing
 * whether stuff is wired up correctly to the editor.
 */
public class BadWordReconcileEngine implements IReconcileEngine {

	static enum BWProblemType implements ProblemType {
		VERY_BAD_WORD,
		BAD_WORD;

		@Override
		public ProblemSeverity getDefaultSeverity() {
			if (this==VERY_BAD_WORD) {
				return ProblemSeverity.ERROR;
			} else {
				return ProblemSeverity.WARNING;
			}
		}

		@Override
		public String getCode() {
			return this.name();
		}
	}

	private final String[] BADWORDS = {
			"bar", "foo"
	};

	public BadWordReconcileEngine() {
	}

	@Override
	public void reconcile(IDocument doc, IProblemCollector problemCollector) {
		String text = doc.get();
		System.out.println(">>>> reconciling for bad words ==========");
		System.out.println(text);
		System.out.println("<<<< reconciling for bad words ==========");

		problemCollector.beginCollecting();
		try {
			for (String badword : BADWORDS) {
				int pos = 0;
				while (pos>=0 && pos < text.length()) {
					int badPos = text.indexOf(badword, pos);
					if (badPos>=0) {
						if (badword.equals(BADWORDS[0])) {
							problemCollector.accept(new ReconcileProblemImpl(BWProblemType.VERY_BAD_WORD, "'"+badword+"' is a VERY bad word", badPos, badword.length()));
						} else {
							problemCollector.accept(new ReconcileProblemImpl(BWProblemType.BAD_WORD, "'"+badword+"' is a bad word", badPos, badword.length()));
						}
						pos = badPos+1;
					} else {
						pos = badPos;
					}
				}
			}
		} finally {
			problemCollector.endCollecting();
		}
	}

}
