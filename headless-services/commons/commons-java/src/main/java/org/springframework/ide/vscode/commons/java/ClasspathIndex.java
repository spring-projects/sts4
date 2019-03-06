/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface ClasspathIndex extends Disposable {

	IType findType(String fqName);
	Flux<Tuple2<String, Double>> fuzzySearchTypes(String searchTerm, boolean includeBinaries, boolean includeSystemLibs);
	Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm, boolean includeBinaries, boolean includeSystemLibs);
	Flux<IType> allSubtypesOf(IType type);
	Flux<IType> allSuperTypesOf(IType type);
	IJavaModuleData findClasspathResourceContainer(String fqName);

}
