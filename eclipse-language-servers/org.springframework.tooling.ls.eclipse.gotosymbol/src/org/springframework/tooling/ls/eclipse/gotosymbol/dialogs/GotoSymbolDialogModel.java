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

import org.eclipse.core.runtime.Assert;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.tooling.ls.eclipse.gotosymbol.GotoSymbolPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableSet;

public class GotoSymbolDialogModel {

	private static final boolean DEBUG = false;//(""+Platform.getLocation()).contains("kdvolder");
	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	@FunctionalInterface
	public interface OKHandler {
		/**
		 * Called by the ui to perform the dialog's action. The dialog will be
		 * closed by the ui this returns true, otherwise it remains open.
		 */
		boolean performOk(SymbolInformation selection);
	}
	
	private static final OKHandler DEFAULT_OK_HANDLER = (selection) -> true;

	private SymbolsProvider[] symbolsProviders;
	private final LiveVariable<String> status = new LiveVariable<>();
	private int currentSymbolsProviderIndex;
	private final LiveVariable<SymbolsProvider> currentSymbolsProvider = new LiveVariable<>(null);
	private final LiveVariable<String> searchBox = new LiveVariable<>("");
	private final ObservableSet<SymbolInformation> unfilteredSymbols = new ObservableSet<SymbolInformation>(ImmutableSet.of(), AsyncMode.ASYNC, AsyncMode.SYNC) {
		//Note: fetching is 'slow' so is done asynchronously
		{
			setRefreshDelay(100);
			dependsOn(searchBox);
			dependsOn(currentSymbolsProvider);
		}
		
		@Override
		protected ImmutableSet<SymbolInformation> compute() {
			status.setValue("Fetching symbols...");
			try {
				SymbolsProvider sp = currentSymbolsProvider.getValue();
				if (sp!=null) {
					debug("Fetching "+sp.getName());
					String query = searchBox.getValue();
					debug("Fetching symbols... from symbol provider, for '"+query+"'");
					Collection<SymbolInformation> fetched = sp.fetchFor(query);
					if (keyBindings==null) {
						status.setValue(sp.getName());
					} else {
						status.setValue("Press ["+keyBindings+"] for "+nextSymbolsProvider().getName());
					}
					return ImmutableSet.copyOf(fetched);
				} else {
					status.setValue("No symbol provider");
				}
			} catch (Exception e) {
				GotoSymbolPlugin.getInstance().getLog().log(ExceptionUtil.status(e));
				status.setValue(ExceptionUtil.getMessage(e));
			}
			return ImmutableSet.of();
		}

		private SymbolsProvider nextSymbolsProvider() {
			int nextIndex = (currentSymbolsProviderIndex + 1)%symbolsProviders.length;
			return symbolsProviders[nextIndex];
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
	
	private String keyBindings;
	private OKHandler okHandler = DEFAULT_OK_HANDLER;

	public GotoSymbolDialogModel(String keyBindings, SymbolsProvider... symbolsProviders) {
		this.keyBindings = keyBindings;
		Assert.isLegal(symbolsProviders.length>0);		
		this.symbolsProviders = symbolsProviders;
		this.currentSymbolsProviderIndex = 0;
		this.currentSymbolsProvider.setValue(symbolsProviders[0]);
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
	
	public synchronized void toggleSymbolsProvider() {
		currentSymbolsProviderIndex = (currentSymbolsProviderIndex+1)%symbolsProviders.length;
		currentSymbolsProvider.setValue(symbolsProviders[currentSymbolsProviderIndex]);
	}

	/**
	 * Set an ok handler. The handler is meant to be called by the UI when user request to
	 * execute the dialogs action on its current selection. For example, by pressing 'ENTER'
	 * key, or by double-clicking an element.
	 * <p>
	 * If 'okHandler' returns 'true' then the dialog is closed. Otherwise it remains open. 
	 */
	public GotoSymbolDialogModel setOkHandler(OKHandler okHandler) {
		this.okHandler = okHandler == null ? DEFAULT_OK_HANDLER : okHandler;
		return this;
	}
	
	public boolean performOk(SymbolInformation selection) {
		return this.okHandler.performOk(selection);
	}
	
	
}
