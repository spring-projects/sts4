package org.springframework.ide.vscode.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.application.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtil;
import org.springframework.ide.vscode.application.properties.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.languageserver.util.LoggingFormat;

import io.typefox.lsapi.services.json.LoggingJsonAdapter;

public class Main {
    private static final Logger LOG = Logger.getLogger("main");

    public static void main(String[] args) throws IOException {
    	LOG.info("Starting LS");
        try {
            LoggingFormat.startLogging();

            Connection connection = connectToNode();

            run(connection);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);

            System.exit(1);
        }
    }

    private static Connection connectToNode() throws IOException {
        String port = System.getProperty("server.port");

        if (port != null) {
            Socket socket = new Socket("localhost", Integer.parseInt(port));

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            OutputStream intercept = new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
            };

            LOG.info("Connected to parent using socket on port " + port);

            return new Connection(in, intercept);
        }
        else {
            InputStream in = System.in;
            PrintStream out = System.out;

            LOG.info("Connected to parent using stdio");

            return new Connection(in, out);
        }
    }

    private static class Connection {
        final InputStream in;
        final OutputStream out;

        private Connection(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }
    }

    /**
     * Listen for requests from the parent node process.
     * Send replies asynchronously.
     * When the request stream is closed, wait for 5s for all outstanding responses to compute, then return.
     */
    public static void run(Connection connection) {
    	//TODO: proper TypeUtilProvider and IndexProvider that somehow determine classpath that should be
    	// in effect for given IDocument and provide TypeUtil or SpringPropertyIndex parsed from that classpath.
    	// Note that the provider is responsible for doing some kind of sensible caching so that indexes are not
    	// rebuilt every time the index is being used.
    	SpringPropertyIndexProvider indexProvider = new DefaultSpringPropertyIndexProvider();
		TypeUtil typeUtil = new TypeUtil(null);
		TypeUtilProvider typeUtilProvider = (IDocument doc) -> typeUtil;
		ApplicationYamlLanguageServer server = new ApplicationYamlLanguageServer(indexProvider, typeUtilProvider);
    	LoggingJsonAdapter jsonServer = new LoggingJsonAdapter(server);
    	jsonServer.setMessageLog(new PrintWriter(System.out));

        jsonServer.connect(connection.in, connection.out);
        jsonServer.getProtocol().addErrorListener((message, err) -> {
            LOG.log(Level.SEVERE, message, err);

            server.onError(message, err);
        });
        
        try {
            jsonServer.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
