/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.Version;

import com.google.common.collect.ImmutableList;

public class UnsupportedVersionDiagnostic extends BootDiagnosticProvider {


	@Override
	public SpringProjectDiagnostic getDiagnostic(IJavaProject project, SpringDependencyInfo dependency, Generations generations) throws Exception {

		URI uri = getBuildFileUri(project);
		if (uri == null) {
			return null;
		}
		
		List<Generation> genList = generations.getGenerations();
		
		// The generation of the current dependency
		Generation dependencyGeneration = null;
		
		int dependencyMajor = dependency.getVersion().getMajor();
		int dependencyMinor = dependency.getVersion().getMinor();
		
		// Collect the latest versions of each major version that are still supported
		List<Generation> latestSupportedPerMajor = new ArrayList<>();
		
		if (genList != null && genList.size() > 1) {

			// The very latest version is first in the list as the generations are ordered by version
			latestSupportedPerMajor.add(genList.get(0));

		   for (int i = 1; i < genList.size(); i++) {
			   Generation toAdd = genList.get(i);
			   Version toAddVersion = getVersion(toAdd);

			   Generation lastAdded = latestSupportedPerMajor.get(latestSupportedPerMajor.size() - 1);
			   Version lastAddedVersion = getVersion(lastAdded);

			   if (isCommercialValid(toAdd) && isOssValid(toAdd) && toAddVersion.getMajor() < lastAddedVersion.getMajor()) {
				   latestSupportedPerMajor.add(toAdd);
			   }
				if (toAddVersion.getMajor() == dependencyMajor 
						&& toAddVersion.getMinor() == dependencyMinor) {
					dependencyGeneration = toAdd;
				}
		   }
		}
		
		if (dependencyGeneration == null) {
			throw new Exception("Unable to find Spring Generation for: " + dependency.getVersion().toString());
		}
		
		StringBuffer message = new StringBuffer();
		DiagnosticSeverity severity = DiagnosticSeverity.Information;

		if (isCommercialValid(dependencyGeneration) && isOssValid(dependencyGeneration)) {
			message.append("OSS support ends on: ");
			message.append(dependencyGeneration.getOssSupportEndDate());
			message.append('\n');
			message.append("Commercial supports ends on: ");
			message.append(dependencyGeneration.getCommercialSupportEndDate());
		} else {
	
			Generation toUpgrade = null;
			
			// Calculate latest supported version for the same major version as the dependency
			// For example, if the dependency version is 1.5.0 and the latest supported is 1.8.0, 
			// find the generation for 1.8.0
			for (Generation generation : latestSupportedPerMajor) {
				Version dependencyVersion = getVersion(dependencyGeneration);
				Version latest = getVersion(generation);
				if (latest.getMajor() == dependencyVersion.getMajor()) {
					toUpgrade = generation;
					break;
				}
			}
			
			if (toUpgrade == null) {
				// if there are no supported versions in the dependency major range, upgrade to the very
				// latest version
				toUpgrade = latestSupportedPerMajor.get(0);
			}
			
			if (isCommercialValid(dependencyGeneration)) {
				severity = DiagnosticSeverity.Warning;
				message.append("Unsupported OSS. Support ended on: ");
				message.append(dependencyGeneration.getOssSupportEndDate());
				message.append('\n');
				message.append("Commercial supports ends on: ");
				message.append(dependencyGeneration.getCommercialSupportEndDate());
				
			} else if (isOssValid(dependencyGeneration)) {
				severity = DiagnosticSeverity.Warning;
				message.append("OSS support ends on: ");
				message.append(dependencyGeneration.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(dependencyGeneration.getCommercialSupportEndDate());
				
			} else {
				// OSS and Commercial support have ended
				severity = DiagnosticSeverity.Error;
				
				message.append("Unsupported OSS. Support ended on: ");
				message.append(dependencyGeneration.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(dependencyGeneration.getCommercialSupportEndDate());
			}
			
			message.append('\n');
			message.append("Please upgrade to a newer supported version: ");
			message.append(getVersion(toUpgrade).toString());
		}
		
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setCode(BOOT_VERSION_VALIDATION_CODE);
		diagnostic.setMessage(message.toString());
		
		Range range = new Range();
		Position start = new Position();
		start.setLine(0);
		start.setCharacter(0);
		range.setStart(start);
		Position end = new Position();
		end.setLine(0);
		end.setCharacter(1);
		range.setEnd(end);
		diagnostic.setRange(range);
		diagnostic.setSeverity(severity);
		
		
		setQuickfix(diagnostic);
		
		return new SpringProjectDiagnostic(diagnostic, uri);
	}


	private void setQuickfix(Diagnostic diagnostic) {
		// TODO: Fix this when open rewrite recipe quickfix becomes available.
		Diagnostic refDiagnostic = new Diagnostic(diagnostic.getRange(), diagnostic.getMessage(), diagnostic.getSeverity(), diagnostic.getSource());
		CodeAction ca = new CodeAction();
		ca.setKind(CodeActionKind.QuickFix);
		ca.setTitle("Validation FIX");
		ca.setDiagnostics(List.of(refDiagnostic));
		String commandId = "";
		ca.setCommand(new Command("Validation FIX", commandId, ImmutableList.of()));
		diagnostic.setData(ca);		
	}


	private boolean isOssValid(Generation gen) {
		
		Date currentDate = new Date(System.currentTimeMillis());
		Date ossEndDate = Date.valueOf(gen.getOssSupportEndDate());
		return currentDate.before(ossEndDate);
	}

	private boolean isCommercialValid(Generation gen) {
		
		Date currentDate = new Date(System.currentTimeMillis());
		Date commercialEndDate = Date.valueOf(gen.getCommercialSupportEndDate());

		return currentDate.before(commercialEndDate);
	}
}
