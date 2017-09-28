/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4j.SymbolInformation;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;

import com.google.common.collect.ImmutableSet;

import org.springframework.ide.eclipse.boot.util.Log;

public class GotoSymbolDialogModel {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}


	//TODO: support for switching between local/workspace symbols.
	
	private final LiveVariable<String> status = new LiveVariable<>();
	private final LiveVariable<SymbolsProvider> symbolsProvider = new LiveVariable<>(null);
	private final LiveVariable<String> searchBox = new LiveVariable<>("");
	private final ObservableSet<SymbolInformation> unfilteredSymbols = new ObservableSet<SymbolInformation>(ImmutableSet.of(), AsyncMode.ASYNC, AsyncMode.SYNC) {
		//Note: fetching is 'slow' so is done asynchronously
		{
			dependsOn(searchBox);
			dependsOn(symbolsProvider);
		}
		
		@Override
		protected ImmutableSet<SymbolInformation> compute() {
			status.setValue("Fetching symbols...");
			try {
				debug("Fetching symbols ");
				SymbolsProvider sp = symbolsProvider.getValue();
				if (sp!=null) {
					String query = searchBox.getValue();
					debug("Fetching symbols... from symbol provider, for '"+query+"'");
					Collection<SymbolInformation> fetched = sp.fetchFor(query);
					if (DEBUG) {
						fetched.stream().forEach(sym -> debug("symbol: "+sym.getName()));
					}
					StringBuilder msg = new StringBuilder();
					msg.append("Fetched ");
					msg.append(fetched.size());
					msg.append(" symbols");
					if (StringUtil.hasText(query)) {
						msg.append(" matching '");
						msg.append(query);
						msg.append("'");
					}
					debug(msg.toString());
					status.setValue(msg.toString());
					return ImmutableSet.copyOf(fetched);
				} else {
					status.setValue("No symbol provider");
				}
			} catch (Exception e) {
				Log.log(e);
				status.setValue(ExceptionUtil.getMessage(e));
			}
			return ImmutableSet.of();
		}
	};
	private ObservableSet<SymbolInformation> filteredSymbols = new ObservableSet<SymbolInformation>() {
		//Note: filtering is 'fast' so is done synchronously
		{
			dependsOn(searchBox);
			dependsOn(unfilteredSymbols);
		}
		
		private boolean containsCharacters(char[] symbolChars, char[] queryChars) {
			int symbolindex = 0;
			int queryindex = 0;

			while (queryindex < queryChars.length && symbolindex < symbolChars.length) {
				if (symbolChars[symbolindex] == queryChars[queryindex]) {
					queryindex++;
				}
				symbolindex++;
			}

			return queryindex == queryChars.length;
		}

		@Override
		protected ImmutableSet<SymbolInformation> compute() {
			char[] query = searchBox.getValue().toCharArray();
			ImmutableSet.Builder<SymbolInformation> builder = ImmutableSet.builder();
			unfilteredSymbols.getValues().stream().filter(sym -> containsCharacters(sym.getName().toCharArray(), query)).forEach(builder::add);
			return builder.build();
		}
	};

	public GotoSymbolDialogModel(SymbolsProvider symbolsProvider) {
		this.symbolsProvider.setValue(symbolsProvider);
		if (DEBUG) {
			searchBox.addListener((e, v) -> {
				debug("searchBox = "+v);
			});
			unfilteredSymbols.addListener((e, v) -> debug("raw = "+summary(filteredSymbols.getValues())));
			filteredSymbols.addListener((e, v) -> debug("filtered = "+summary(filteredSymbols.getValues())));
		}
	}

	private List<String> summary(ImmutableSet<SymbolInformation> values) {
		return values.stream().map(SymbolInformation::getName).collect(Collectors.toList());
	}

	public ObservableSet<SymbolInformation> getSymbols() {
		return filteredSymbols;
	}

	public LiveVariable<String> getSearchBox() {
		return searchBox;
	}

	public LiveExpression<String> getStatus() {
		return status;
	}
	
}
