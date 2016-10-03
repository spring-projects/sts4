package org.springframework.ide.vscode.util;

import io.typefox.lsapi.MessageParams;
import io.typefox.lsapi.impl.MessageParamsImpl;
import io.typefox.lsapi.MessageType;

public class ShowMessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public final MessageParams message;

    public ShowMessageException(MessageParams message, Exception cause) {
        super(message.getMessage(), cause);

        this.message = message;
    }

    public static ShowMessageException error(String message, Exception cause) {
        return create(MessageType.Error, message, cause);
    }

    public static ShowMessageException warning(String message, Exception cause) {
        return create(MessageType.Warning, message, cause);
    }
    
    private static ShowMessageException create(MessageType warning, String message, Exception cause) {
        MessageParamsImpl m = new MessageParamsImpl();

        m.setMessage(message);
        m.setType(warning);

        return new ShowMessageException(m, cause);
    }
}
