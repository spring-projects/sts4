package org.springframework.ide.vscode.boot.java.cron;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;

public class CronExpressionCompletionProvider implements AnnotationAttributeCompletionProvider {
	
	 private static final Map<String, String> CRON_EXPRESSIONS_MAP = new LinkedHashMap<>();

	    static {
	        CRON_EXPRESSIONS_MAP.put("0 0 * * * 1-5", "every hour every day between Monday and Friday");
	        CRON_EXPRESSIONS_MAP.put("0 */5 * * * *", "every 5 minutes");
	        CRON_EXPRESSIONS_MAP.put("0 * * * * *", "every minute");
	        CRON_EXPRESSIONS_MAP.put("0 0 */6 * * *", "every 6 hours at minute 0");
	        CRON_EXPRESSIONS_MAP.put("0 0 * * * *", "every hour");
	        CRON_EXPRESSIONS_MAP.put("0 0 * * * SUN", "every hour at Sunday day");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 * * *", "at 00:00");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 * * SAT,SUN", "at 00:00 on Saturday and Sunday");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 * * 6,0", "at 00:00 at Saturday and Sunday days");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 1-7 * SUN", "at 00:00 every day between 1 and 7 at Sunday day");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 1 * *", "at 00:00 at 1 day");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 1 1 *", "at 00:00 at 1 day at January month");
	        CRON_EXPRESSIONS_MAP.put("0 0 8-18 * * *", "every hour between 8 and 18");
	        CRON_EXPRESSIONS_MAP.put("0 0 9 * * MON", "at 09:00 at Monday day");
	        CRON_EXPRESSIONS_MAP.put("0 0 10 * * *", "at 10:00");
	        CRON_EXPRESSIONS_MAP.put("0 30 9 * JAN MON", "at 09:30 at January month at Monday day");
	        CRON_EXPRESSIONS_MAP.put("10 * * * * *", "every minute at second 10");
	        CRON_EXPRESSIONS_MAP.put("0 0 8-10 * * *", "every hour between 8 and 10");
	        CRON_EXPRESSIONS_MAP.put("0 0/30 8-10 * * *", "every 30 minutes every hour between 8 and 10");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 L * *", " at 00:00 last day of month");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 1W * *", "at 00:00 the nearest weekday to the 1 of the month");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 * * THUL", "at 00:00 last Thursday of every month");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 ? * 5#2", "at 00:00 Friday 2 of every month");
	        CRON_EXPRESSIONS_MAP.put("0 0 0 ? * MON#1", "at 00:00 Monday 1 of every month");
	    }


    @Override
    public Map<String, String> getCompletionCandidatesWithLabels(IJavaProject project) {
        return CRON_EXPRESSIONS_MAP;
    }
}
