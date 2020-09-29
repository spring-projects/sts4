/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 * Reconciler responsible for creating annotation at application names positions
 * in the Deployment YAML document
 *
 * @author Alex Boyko
 *
 */
public class AppNameReconciler {

	/**
	 * YAML parser
	 */
	private YamlASTProvider fParser;

	public AppNameReconciler(YamlASTProvider parser) {
		fParser = parser;
	}

	/**
	 * Re-populates annotation model with app name annotations based on document contents
	 *
	 * @param document The YAML document
	 * @param annotationModel Application Names annotation model
	 * @param monitor Progress monitor
	 */
	public void reconcile(IDocument document, AppNameAnnotationModel annotationModel, IProgressMonitor monitor) {
		if (annotationModel == null) {
			return;
		}

		List<Annotation> toRemove= new ArrayList<>();

		Iterator<? extends Annotation> iter= annotationModel.getAnnotationIterator();
		while (iter.hasNext()) {
			Annotation annotation= iter.next();
			if (AppNameAnnotation.TYPE.equals(annotation.getType())) {
				toRemove.add(annotation);
			}
		}
		Annotation[] annotationsToRemove= toRemove.toArray(new Annotation[toRemove.size()]);

		/*
		 * Create brand new annotation to position map based on docs contents
		 */
		Map<AppNameAnnotation, Position> annotationsToAdd = createAnnotations(document, annotationModel, monitor);

		/*
		 * Update annotation model
		 */
		if (annotationModel instanceof IAnnotationModelExtension)
			((IAnnotationModelExtension)annotationModel).replaceAnnotations(annotationsToRemove, annotationsToAdd);
		else {
			for (int i= 0; i < annotationsToRemove.length; i++)
				annotationModel.removeAnnotation(annotationsToRemove[i]);
			for (iter= annotationsToAdd.keySet().iterator(); iter.hasNext();) {
				Annotation annotation= iter.next();
				annotationModel.addAnnotation(annotation, annotationsToAdd.get(annotation));
			}
		}
	}

	/**
	 * Create new annotation to position mapping based on the document contents
	 *
	 * @param annotationModel Application name annotations model
	 * @return Map of annotations to their corresponding positions
	 */
	private Map<AppNameAnnotation, Position> createAnnotations(IDocument document, AppNameAnnotationModel annotationModel, IProgressMonitor monitor) {
		Map<AppNameAnnotation, Position> annotationsMap = new LinkedHashMap<>();
		monitor.beginTask("Calculating application names", 100);
		try {
			YamlFileAST ast = fParser.getAST(document);
			String contents = document.get();
			List<Node> rootList = ast.getNodes();
			monitor.worked(70);
			if (rootList.size() == 1) {
				Node root = rootList.get(0);
				SequenceNode applicationsNode = YamlGraphDeploymentProperties.getNode(root, ApplicationManifestHandler.APPLICATIONS_PROP, SequenceNode.class);
				if (applicationsNode == null) {
					/*
					 * No 'applications' YAML node consider root elements to the deployment properties of an application
					 */
					ScalarNode node = YamlGraphDeploymentProperties.getPropertyValue(root, ApplicationManifestHandler.NAME_PROP, ScalarNode.class);
					if (node != null) {
						/*
						 * There is 'name' property present, so yes root has application deployment props
						 */
						annotationsMap.put(new AppNameAnnotation(node.getValue(), true),
								new Position(root.getStartMark().getIndex(),
										getLastWhiteCharIndex(contents, root.getEndMark().getIndex())
												- root.getStartMark().getIndex()));
					}
				} else {
					/*
					 * Go through entries in the 'applications' sequence node
					 */
					for (Node appNode : applicationsNode.getValue()) {
						ScalarNode node = YamlGraphDeploymentProperties.getNode(appNode, ApplicationManifestHandler.NAME_PROP, ScalarNode.class);
						if (node != null) {
							/*
							 * Add application name annotation entry
							 */
							annotationsMap.put(new AppNameAnnotation(node.getValue()), new Position(appNode.getStartMark().getIndex(), getLastWhiteCharIndex(contents, appNode.getEndMark().getIndex()) - appNode.getStartMark().getIndex()));
						}
					}
				}
				monitor.worked(20);
				if (!annotationsMap.isEmpty()) {
					if (annotationModel.fixedAppName == null) {
						/*
						 * Select either previously selected app name annotation or the first found
						 */
						reselectAnnotation(annotationModel, annotationsMap);
					} else {
						/*
						 * Select annotation corresponding to application name == to fAppName
						 */
						selectAnnotationByAppName(annotationsMap, annotationModel.fixedAppName);
					}
					monitor.worked(10);
				}
			}
		} catch (ParserException | ScannerException e) {
			// Ignore these exceptions as they'd appear as syntax errors in the editor
		} catch (Throwable t) {
			Log.log(t);
		} finally {
			monitor.done();
		}
		return annotationsMap;
	}

	/**
	 * Selects annotation from the map corresponding to currently selected
	 * annotation. Otherwise just selects the first found annotation
	 *
	 * @param annotationModel Application name annotations model
	 * @param annotationsMap Map of application name annotations to positions
	 */
	private void reselectAnnotation(AppNameAnnotationModel annotationModel, Map<AppNameAnnotation, Position> annotationsMap) {
		AppNameAnnotation selected = annotationModel.getSelectedAppAnnotation();
		Map.Entry<AppNameAnnotation, Position> newSelected = null;
		if (selected != null) {
			Position selectedPosition = annotationModel.getPosition(selected);
			for (Map.Entry<AppNameAnnotation, Position> entry : annotationsMap.entrySet()) {
				/*
				 * Check if application name matches
				 */
				if (entry.getKey().getText().equals(selected.getText())) {
					/*
					 * If name matches see if previous match is further away
					 * from previously selected annotation offset than the
					 * current match. Update the match accordingly.
					 */
					if (newSelected == null) {
						newSelected = entry;
					} else if (Math.abs(newSelected.getValue().getOffset() - selectedPosition.getOffset()) > Math.abs(entry.getValue().getOffset() - selectedPosition.getOffset())){
						newSelected = entry;
					}
				} else if (entry.getValue().getOffset() == selectedPosition.getOffset() && newSelected == null) {
					newSelected = entry;
				}
			}
		}
		if (newSelected == null) {
			/*
			 * No matches found. Select the first annotation to have something selected.
			 */
			newSelected = annotationsMap.entrySet().iterator().next();
		}
		newSelected.getKey().markSelected();
	}

	/**
	 * Select annotation matching constant application name, i.e. <code>FAppName</code>
	 *
	 * @param annotationsMap Map of application name annotations to positions
	 */
	private void selectAnnotationByAppName(Map<AppNameAnnotation, Position> annotationsMap, String appName) {
		for (Map.Entry<AppNameAnnotation, Position> entry : annotationsMap.entrySet()) {
			if (entry.getKey().getText().equals(appName)) {
				entry.getKey().markSelected();
				return;
			}
		}
	}

	/**
	 * Returns the first 'white' char after a word appearing before the passed index
	 *
	 * @param text Text
	 * @param index Index to start looking from
	 * @return The first 'white' char position in a string
	 */
	private static int getLastWhiteCharIndex(String text, int index) {
		if (index == text.length()) {
			return index;
		}
		int i = index;
		for (; i >=  0 && Character.isWhitespace(text.charAt(i)); i--) {
			// Nothing to do
		}
		// Special case: if non white char is at position 'index' then return value of 'index'
		return i == index ? i : i + 1;
	}

}
