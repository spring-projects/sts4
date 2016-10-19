package org.springframework.ide.vscode.commons.languageserver.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.StringJoiner;
import java.util.logging.*;

public class LoggingFormat extends Formatter {
    public static ThreadLocal<Integer> request = new ThreadLocal<>();

    @Override
    public String format(LogRecord record) {
        StringJoiner joiner = new StringJoiner("\t");

        joiner.add(record.getLevel().getName());
        joiner.add(requestAsString());
        joiner.add(Thread.currentThread().getName());
        joiner.add(Instant.ofEpochMilli(record.getMillis()).toString());
//        joiner.add(record.getLoggerName());
        joiner.add(record.getSourceClassName() + "#" + record.getSourceMethodName());
        joiner.add(record.getMessage());

        String result = joiner.toString() + "\n";

        Throwable thrown = record.getThrown();

        if (thrown != null) {
            StringWriter stackTrace = new StringWriter();
            PrintWriter print = new PrintWriter(stackTrace);

            thrown.printStackTrace(print);
            print.flush();

            result = result + stackTrace + "\n";
        }

        return result;
    }

    private CharSequence requestAsString() {
        Integer i = request.get();

        if (i == null)
            return "?";
        else
            return Integer.toString(i);
    }

    private static boolean started = false;

    public static void startLogging() throws IOException {
        if (!started) {
            started = true;

            Logger root = Logger.getLogger("");

            FileHandler file = new FileHandler("javac-services.%g.log", 100_000, 1, false);

            root.addHandler(file);

            for (Handler h : root.getHandlers())
                h.setFormatter(new LoggingFormat());
        }
    }
}
