/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageComputer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class LanguageComputerTest {
	
	@Autowired
	LanguageComputer languageComputer;
	
	@Test
	void jdtUri() {
		assertThat(languageComputer).isNotNull();
		URI uri = URI.create("jdt://contents/spring-data-commons-1.11.4.RELEASE.jar/org.springframework.data.mapping.model/PropertyNameFieldNamingStrategy.class?%3Dboot13_with_mongo%2F%5C%2FUsers%5C%2Faboyko%5C%2F.m2%5C%2Frepository%5C%2Forg%5C%2Fspringframework%5C%2Fdata%5C%2Fspring-data-commons%5C%2F1.11.4.RELEASE%5C%2Fspring-data-commons-1.11.4.RELEASE.jar%3Corg.springframework.data.mapping.model%28PropertyNameFieldNamingStrategy.class");
		assertThat(languageComputer.computeLanguage(uri)).isEqualTo(LanguageId.CLASS);
		
		uri = URI.create("file:///project/org.springframework.data.mapping.model/PropertyNameFieldNamingStrategy.java");
		assertThat(languageComputer.computeLanguage(uri)).isEqualTo(LanguageId.JAVA);

	}

}
