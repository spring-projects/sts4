/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.pom.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.junit.Test;
import org.springframework.ide.eclipse.maven.pom.DomStructureComparable;
import org.springframework.ide.eclipse.maven.pom.DomStructureComparable.DomType;
import org.springframework.ide.eclipse.maven.pom.IdProviderRegistry;
import org.springframework.ide.eclipse.maven.pom.XMLStructureCreator;

public class PomStructureCreatorTest {
	
	@Test
	public void smokeTest() throws BadLocationException {
		String xml = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"    <relativePath>../../</relativePath>\n" + 
				"  </parent>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"</project>";
		DomStructureComparable result = parse(xml);
		assertRange(0, xml.length(), result);
		
		assertEquals(1, result.getChildren().length);
		
		DomStructureComparable project = (DomStructureComparable) result.getChildren()[0];
		
		assertRange(0, xml.length(), project);
		
		assertEquals(6, project.getChildren().length);
		
		DomStructureComparable modelVersion = (DomStructureComparable) project.getChildren()[0];
		
		assertEquals(DomType.ELEMENT.toString(), modelVersion.getType());
		
		assertRangeText("  <modelVersion>4.0.0</modelVersion>\n", modelVersion);
	}
	
	@Test
	public void domNodeRange_1() throws Exception {
		String xml = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"<modelVersion>4.0.0</modelVersion>   \n" + 
				"</project>";
		DomStructureComparable result = parse(xml);
		assertRange(0, xml.length(), result);
		
		assertEquals(1, result.getChildren().length);
		
		DomStructureComparable project = (DomStructureComparable) result.getChildren()[0];
		
		assertRange(0, xml.length(), project);
		
		assertEquals(1, project.getChildren().length);
		
		DomStructureComparable modelVersion = (DomStructureComparable) project.getChildren()[0];
		
		assertRangeText("<modelVersion>4.0.0</modelVersion>   \n", modelVersion);
	}

	@Test
	public void domNodeRange_2() throws Exception {
		String xml = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"   <modelVersion>4.0.0</modelVersion>" + 
				"</project>";
		DomStructureComparable result = parse(xml);
		assertRange(0, xml.length(), result);
		
		assertEquals(1, result.getChildren().length);
		
		DomStructureComparable project = (DomStructureComparable) result.getChildren()[0];
		
		assertRange(0, xml.length(), project);
		
		assertEquals(1, project.getChildren().length);
		
		DomStructureComparable modelVersion = (DomStructureComparable) project.getChildren()[0];
		
		assertRangeText("   <modelVersion>4.0.0</modelVersion>", modelVersion);
	}
	
	@Test
	public void domNodeRange_3() throws Exception {
		String xml = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">" + 
				"<modelVersion>4.0.0</modelVersion>  \n" + 
				"</project>";
		DomStructureComparable result = parse(xml);
		assertRange(0, xml.length(), result);
		
		assertEquals(1, result.getChildren().length);
		
		DomStructureComparable project = (DomStructureComparable) result.getChildren()[0];
		
		assertRange(0, xml.length(), project);
		
		assertEquals(1, project.getChildren().length);
		
		DomStructureComparable modelVersion = (DomStructureComparable) project.getChildren()[0];
		
		assertRangeText("<modelVersion>4.0.0</modelVersion>  \n", modelVersion);
	}
	
	@Test
	public void domNodeRange_4() throws Exception {
		String xml = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"   \n" + 
				"  <modelVersion>4.0.0</modelVersion>  \n" + 
				"</project>";
		DomStructureComparable result = parse(xml);
		assertRange(0, xml.length(), result);
		
		assertEquals(1, result.getChildren().length);
		
		DomStructureComparable project = (DomStructureComparable) result.getChildren()[0];
		
		assertRange(0, xml.length(), project);
		
		assertEquals(1, project.getChildren().length);
		
		DomStructureComparable modelVersion = (DomStructureComparable) project.getChildren()[0];
		
		assertRangeText("  <modelVersion>4.0.0</modelVersion>  \n", modelVersion);
	}
	
	private void assertRangeText(String expectedText, DomStructureComparable modelVersion) throws BadLocationException {
		String actualText = modelVersion.getDocument().get(modelVersion.getRange().offset, modelVersion.getRange().length);
		assertEquals(expectedText, actualText);
	}

	private void assertRange(int start, int end, DomStructureComparable structure) {
		Position range = structure.getRange();
		assertEquals(start, range.offset);
		assertEquals(end, range.offset + range.length);
	}

	private DomStructureComparable parse(String xml) {
		XMLStructureCreator creator = new XMLStructureCreator(new IdProviderRegistry());
		DomStructureComparable structure = creator.createStructure(new Document(xml));
		assertNotNull(structure);
		assertEquals(DomType.ROOT.toString(), structure.getType());
		return structure;
	}
	
	
}
