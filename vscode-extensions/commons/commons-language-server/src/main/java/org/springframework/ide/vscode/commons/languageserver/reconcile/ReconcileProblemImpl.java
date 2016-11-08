package org.springframework.ide.vscode.commons.languageserver.reconcile;

import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;

/**
 * An implementation of {@link ReconcileProblem} that is just a simple data object.
 *
 * @author Kris De Volder
 */
public class ReconcileProblemImpl implements ReconcileProblem {

	final private ProblemType type;
	final private String msg;
	final private int offset;
	final private int len;

	public ReconcileProblemImpl(ProblemType type, String msg, int offset, int len) {
		super();
		this.type = type;
		this.msg = msg;
		this.offset = offset;
		this.len = len;
	}

	@Override
	public ProblemType getType() {
		return type;
	}

	@Override
	public String getMessage() {
		return msg;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return len;
	}

	@Override
	public String getCode() {
		return getType().getCode();
	}

	/**
	 * Attempt to enlarge a empty document region to include a
	 * character that can be visibly underlined.
	 */
	protected static DocumentRegion makeVisible(DocumentRegion region) {
		DocumentRegion altRegion = region.textAfter(1);
		if (!altRegion.isEmpty() && canUnderline(altRegion.charAt(0))) {
			return altRegion;
		}
		altRegion = region.textBefore(1);
		if (!altRegion.isEmpty() && canUnderline(altRegion.charAt(0))) {
			return altRegion;
		}
		return region;
	}

	private static boolean canUnderline(char c) {
		return c!='\n'&&c!='\r';
	}

}
