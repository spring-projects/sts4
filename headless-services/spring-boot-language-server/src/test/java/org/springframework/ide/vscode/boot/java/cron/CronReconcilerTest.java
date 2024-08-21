package org.springframework.ide.vscode.boot.java.cron;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.languageserver.reconcile.BasicProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class CronReconcilerTest {
	
	private CronReconciler reconciler = new CronReconciler();
	private List<ReconcileProblem> problems;
	private IProblemCollector collector;
	
	@BeforeEach
	void setup() {
		problems = new ArrayList<>();
		collector = new BasicProblemCollector(problems);
	}
	
	@Test
	void noProblems_1() {
		reconciler.reconcile("0 0 0 L-3 * *", 0, collector);
		assertEquals(0, problems.size());
	}

	@Test
	void DayOfTheWeekProblems_1() {
		reconciler.reconcile("0 0 0 8 * MAR-JUL", 0, collector);
		assertEquals(1, problems.size());
		assertReconcileProblem(problems.get(0), CronProblemType.FIELD, 10, 6);
	}

	@Test
	void noProblems_3() {
		reconciler.reconcile("MAR-JUL 0 0 8 * *", 0, collector);
		assertEquals(1, problems.size());
		assertReconcileProblem(problems.get(0), CronProblemType.FIELD, 0, 7);
	}

	@Test
	void syntax_and_field_problems_1() {
		reconciler.reconcile("0 0 0 8LW * MARCH-JUL", 0, collector);
		assertEquals(3, problems.size());
		assertReconcileProblem(problems.get(0), CronProblemType.SYNTAX, 7, 2);
		assertReconcileProblem(problems.get(1), CronProblemType.SYNTAX, 12, 5);		
		assertReconcileProblem(problems.get(2), CronProblemType.FIELD, 18, 2);		
	}
	
	@Test
	void syntax_problems_2() {
		reconciler.reconcile("qq#3 0 Blah 1-88LW * JUL-MARCH", 0, collector);
		assertEquals(1, problems.size());
		assertReconcileProblem(problems.get(0), CronProblemType.SYNTAX, 0, 2);
	}
	
	@Test
	void syntax_problems_3() {
		reconciler.reconcile("10/2. * * ? * MON-5", 0, collector);
		assertEquals(1, problems.size());
		assertReconcileProblem(problems.get(0), CronProblemType.SYNTAX, 4, 0);
	}
	
	static void assertReconcileProblem(ReconcileProblem p, ProblemType type, int offset, int length) {
		assertEquals(offset, p.getOffset());
		assertEquals(length, p.getLength());
		assertEquals(type, p.getType());
	}
}
