package org.springframework.ide.vscode.commons.rewrite;

import java.nio.charset.StandardCharsets;

import org.openrewrite.shaded.jgit.diff.EditList;
import org.openrewrite.shaded.jgit.diff.HistogramDiff;
import org.openrewrite.shaded.jgit.diff.RawText;
import org.openrewrite.shaded.jgit.diff.RawTextComparator;

class JGitUtils {

	public static EditList getDiff(String txt1, String txt2) {
		RawText rt1 = new RawText(txt1.getBytes(StandardCharsets.UTF_8));
		RawText rt2 = new RawText(txt2.getBytes(StandardCharsets.UTF_8));
		EditList diffList = new EditList();
		diffList.addAll(new HistogramDiff().diff(RawTextComparator.DEFAULT, rt1, rt2));
		return diffList;
	}

}
