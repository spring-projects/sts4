package org.springframework.ide.eclipse.boot.dash.cf.debug;

import org.springsource.ide.eclipse.commons.livexp.core.OnDispose;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

public interface SshTunnel extends Disposable, OnDispose {

	int getLocalPort();

	boolean isDisposed();

}
