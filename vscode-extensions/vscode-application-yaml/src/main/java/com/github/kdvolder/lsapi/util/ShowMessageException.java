package com.github.kdvolder.lsapi.util;

import io.typefox.lsapi.MessageParams;
import io.typefox.lsapi.MessageParamsImpl;

public class ShowMessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public final MessageParams message;

    public ShowMessageException(MessageParams message, Exception cause) {
        super(message.getMessage(), cause);

        this.message = message;
    }

    public static ShowMessageException error(String message, Exception cause) {
        return create(MessageParams.TYPE_ERROR, message, cause);
    }

    public static ShowMessageException warning(String message, Exception cause) {
        return create(MessageParams.TYPE_WARNING, message, cause);
    }
    
    private static ShowMessageException create(int type, String message, Exception cause) {
        MessageParamsImpl m = new MessageParamsImpl();

        m.setMessage(message);
        m.setType(type);

        return new ShowMessageException(m, cause);
    }
}
