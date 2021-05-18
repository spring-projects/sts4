package org.springframework.ide.vscode.commons.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.commons.util.SimpleGlob.Match;

public class SimpleGlobTests {
	
	@Test
	public void testing() throws Exception {
		expect("*", "anything", Match.SUCCESS);
		expect("*", "", Match.SUCCESS);
		expect("abc**aaa", "abcXXaaa", Match.SUCCESS);
		
		expect("start-*", "start-", Match.SUCCESS);
		expect("start-*", "start-andmore", Match.SUCCESS);
		expect("start-*", "start", Match.FAIL);
		
		expect("*-end", "-end", Match.SUCCESS);
		expect("*-end", "andmore-end", Match.SUCCESS);
		expect("*-end", "end", Match.FAIL);
		
		expect("start-*-end", "start--end", Match.SUCCESS);
		expect("start-*-end", "start-middle-end", Match.SUCCESS);
		expect("start-*-end", "start-end", Match.FAIL);
		expect("start-*-end", "start-middle", Match.FAIL);
		expect("start-*-end", "-middle-end", Match.FAIL);
		expect("start-*-end", "astart-middle-end", Match.FAIL);
		expect("start-*-end", "start-middle-enda", Match.FAIL);
	}
	
	@Test
	public void multiStar() throws Exception {
		expect("start-*part*art", "start-part", Match.FAIL);
		expect("start-*-part-*-end", "start-XXX-part-YYY-end", Match.SUCCESS);
		expect("start-*-part-*-end", "start--part--end", Match.SUCCESS);
		expect("start-*part*art", "start-part-part", Match.SUCCESS);
	}
	
	@Test
	public void complexCases() throws Exception {
		expectUnkown(
				"?at", 
				"[abc]at", 
				"[!abc]at", 
				"[a-c]at",
				"[!a-c]at",
				"{cat,bat,[fr]at}"
		);
	}

	private void expectUnkown(String... patterns) {
		for (String p : patterns) {
			expect(p, "whatever", Match.UNKNOWN);
		}
	}

	private void expect(String pattern, String data, Match expected) {
		Match actual = SimpleGlob.create(pattern).match(data);
		assertEquals(expected, actual);
	}

}
