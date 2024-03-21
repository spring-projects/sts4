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
package org.springframework.ide.eclipse.boot.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.eclipse.text.edits.ReplaceEdit;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.events.CommentEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.StreamEndEvent;

public class ConvertYamlToPropertiesRefactoring extends Refactoring {

	private static final String YAML_EXT = ".yml";
	private static final String PROPERTIES_EXT = ".properties";

	private IFile propsFile;
	private final IFile yamlFile;
	private StringBuilder propsContent;
	private int inputTextLen;

	public ConvertYamlToPropertiesRefactoring(IFile yamlFile) {
		this.yamlFile = yamlFile;
		this.propsContent = new StringBuilder();
	}

	@Override
	public String getName() {
		return "Convert .yaml to .properties";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		if (!yamlFile.isAccessible()) {
			return RefactoringStatus.createFatalErrorStatus("The resource '"+yamlFile.getFullPath()+"' is not accessible");
		}
		this.propsFile = propsFileFor(yamlFile);
		if (propsFile.exists()) {
			return RefactoringStatus.createFatalErrorStatus("File '"+propsFile.getFullPath()+"' already exists!");
		}
		return new RefactoringStatus();
	}

	private static IFile propsFileFor(IFile yamlFile) {
		IContainer container = yamlFile.getParent();
		String name = yamlFile.getName();
		if (name.endsWith(YAML_EXT)) {
			name = name.substring(0, name.length()-YAML_EXT.length())+PROPERTIES_EXT;
		} else {
			name = name + PROPERTIES_EXT;
		}
		return container.getFile(Path.EMPTY.append(name));
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			if (hasComments(yamlFile)) {
				status.merge(RefactoringStatus.createWarningStatus("The yaml file has comments, which will be lost in the refactoring!"));
			}
		} catch (Exception e) {
			status.merge(RefactoringStatus.create(ExceptionUtil.status(e, "Problems reading file: "+yamlFile.getFullPath())));
			return status;
		}

		try (InputStream content = yamlFile.getContents()) {
			for (Object d : new Yaml().loadAll(yamlFile.getContents())) {
				if (d instanceof Map) {
					// Add doc divider if not empty
					@SuppressWarnings("unchecked")
					Map<String, ?> o = (Map<String, ?>) d;
					try {
						YamlToPropertiesConverter converter = new YamlToPropertiesConverter(o);
						Properties props = converter.getProperties();
						StringWriter write = new StringWriter();
						props.store(write, null);
						write.flush();
						write.close();
						if (!propsContent.isEmpty()) {
							propsContent.append("#---\n");
						}
						// Skip over the date header. Comments are not present but date header is.
						if (write.getBuffer().charAt(0) == '#') {
							int idx = write.getBuffer().indexOf("\n");
							this.propsContent.append(idx >= 0 && idx < write.getBuffer().length() ? write.getBuffer().substring(idx + 1) : write.getBuffer().toString());
						} else {
							this.propsContent.append(write.getBuffer().toString());
						}
						status.merge(converter.getStatus());
					} catch (IOException e) {
						status.merge(RefactoringStatus.create(ExceptionUtil.status(e, "Problems writing to .properties file: "+propsFile.getFullPath())));
					}
				} else if (d == null) {
					if (!propsContent.isEmpty()) {
						propsContent.append("#---\n");
					}
				}
			}
		} catch (Exception e) {
			status.merge(RefactoringStatus.create(ExceptionUtil.status(e, "Problems parsing as a .yaml file: "+yamlFile.getFullPath())));
		}
		return status;
	}

	private boolean hasComments(IFile yamlFile) throws Exception {
		InputStream is = null;
		try {
			is = yamlFile.getContents();
			LoaderOptions loaderOptions = new LoaderOptions();
			loaderOptions.setProcessComments(true);
			boolean hasComments = false;
			for (Event e : new Yaml(loaderOptions).parse(new InputStreamReader(is))) {
				if (e instanceof StreamEndEvent) {
					inputTextLen = e.getEndMark().getIndex();
				}
				if (!hasComments && e instanceof CommentEvent ce) {
					if (ce.getCommentType() == CommentType.BLANK_LINE) {
						// document separator
					} else {
						hasComments = true;
					}
				}
			}
			return hasComments;
		} catch (Throwable t) {
			if (is != null) {
				is.close();
			}
			return true;
		}
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		CompositeChange changes = new CompositeChange(getName());
		TextFileChange textChange = new TextFileChange(getName(), yamlFile);
		textChange.setEdit(new ReplaceEdit(0, inputTextLen, propsContent.toString()));
		changes.add(textChange);
		changes.add(new RenameResourceChange(yamlFile.getFullPath(), propsFile.getName()));
		changes.initializeValidationData(pm);
		return changes;
	}

}
