/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;

/**
 * @author Martin Lippert
 */
public class RouteUtils {
	
	public static EnhancedSymbolInformation createRouteSymbol(Location location, String path,
			String[] httpMethods, String[] contentTypes, String[] acceptTypes, SymbolAddOnInformation[] enhancedInformation) {
		
		if (path != null && path.length() > 0) {
			String label = "@" + (path.startsWith("/") ? path : ("/" + path));
			label += (httpMethods == null || httpMethods.length == 0 ? "" : " -- " + WebfluxUtils.getStringRep(httpMethods, string -> string));
			
			String acceptType = WebfluxUtils.getStringRep(acceptTypes, WebfluxUtils::getMediaType);
			label += acceptType != null ? " - Accept: " + acceptType : "";
			
			String contentType = WebfluxUtils.getStringRep(contentTypes, WebfluxUtils::getMediaType);
			label += contentType != null ? " - Content-Type: " + contentType : "";

			return new EnhancedSymbolInformation(new SymbolInformation(label, SymbolKind.Interface, location), enhancedInformation);
		}
		else {
			return null;
		}
		
	}

}
