/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.springframework.tooling.ls.eclipse.gotosymbol.GotoSymbolPlugin;
import org.springframework.tooling.ls.eclipse.gotosymbol.favourites.FavouritesPreference;
import org.springsource.ide.eclipse.commons.core.util.FuzzyMatcher;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.HighlightedText;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings("restriction")
public class GotoSymbolDialogModel {
	
	public static class Favourite {
		public final String name; //Descriptive name 
		public final String query; //The 'search string' it corresponds to
		public Favourite(String name, String query) {
			super();
			this.name = name;
			this.query = query;
		}
		
		@Override
		public String toString() {
			return query + " ("+name+")";
		}
	}
	
	public static class Match<T> {
		final double score;
		final String query;
		final T value;
		public Match(double score, String query, T value) {
			super();
			this.score = score;
			this.query = query;
			this.value = value;
		}
		
	}
	
	public static Comparator<Match<Either<SymbolInformation, DocumentSymbol>>> MATCH_COMPARATOR = (m1, m2) -> {
		int comp = Double.compare(m2.score, m1.score);
		if (comp!=0) return comp;
		
		String m1Name = m1.value.isLeft() ? m1.value.getLeft().getName() : m1.value.getRight().getName();
		String m2Name = m2.value.isLeft() ? m2.value.getLeft().getName() : m2.value.getRight().getName();
		
		return m1Name.compareTo(m2Name);
	};

	private static final String SEARCH_BOX_HINT_MESSAGE = "@/ -> request mappings, @+ -> beans, @> -> functions, @ -> all spring elements";
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
	
	public static final OKHandler OPEN_IN_EDITOR_OK_HANDLER = symbolInformation -> {
		if (symbolInformation!=null) {
			Location location = symbolInformation.getLocation();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			LSPEclipseUtils.openInEditor(location, page);
		}
		return true;
	};

	private SymbolsProvider[] symbolsProviders;
	private final LiveVariable<HighlightedText> status = new LiveVariable<>();
	private int currentSymbolsProviderIndex;
	public final LiveVariable<SymbolsProvider> currentSymbolsProvider = new LiveVariable<>(null);
	private final LiveVariable<String> searchBox = new LiveVariable<>("");
	public final ObservableSet<Either<SymbolInformation, DocumentSymbol>> unfilteredSymbols = new ObservableSet<Either<SymbolInformation, DocumentSymbol>>(ImmutableSet.of(), AsyncMode.ASYNC, AsyncMode.SYNC) {
		//Note: fetching is 'slow' so is done asynchronously
		{
			setRefreshDelay(100);
			dependsOn(searchBox);
			dependsOn(currentSymbolsProvider);
		}
		
		@Override
		protected ImmutableSet<Either<SymbolInformation, DocumentSymbol>> compute() {
			status.setValue(HighlightedText.plain("Fetching symbols..."));
			try {
				SymbolsProvider sp = currentSymbolsProvider.getValue();
				if (sp!=null) {
					String currentProviderName = sp.getName();
					debug("Fetching "+currentProviderName);
					String query = searchBox.getValue();
					debug("Fetching symbols... from symbol provider, for '"+query+"'");
					Collection<Either<SymbolInformation, DocumentSymbol>> fetched = sp.fetchFor(query);
					if (keyBindings==null) {
						status.setValue(HighlightedText.plain(currentProviderName));
					} else {
						status.setValue(HighlightedText
									.create()
									.appendHighlight("Showing ")
									.appendHighlight(currentProviderName)
									.appendPlain(". Press [" + keyBindings + "] for ")
									.appendPlain(nextSymbolsProvider().getName())
								);
					}
					return ImmutableSet.copyOf(fetched);
				} else {
					status.setValue(HighlightedText.plain("No symbol provider"));
				}
			} catch (Exception e) {
				GotoSymbolPlugin.getInstance().getLog().log(ExceptionUtil.status(e));
				status.setValue(HighlightedText.plain(ExceptionUtil.getMessage(e)));
			}
			return ImmutableSet.of();
		}

		private SymbolsProvider nextSymbolsProvider() {
			int nextIndex = (currentSymbolsProviderIndex + 1)%symbolsProviders.length;
			return symbolsProviders[nextIndex];
		}
	};
	
	private LiveExpression<Collection<Match<Either<SymbolInformation, DocumentSymbol>>>> filteredSymbols = new LiveExpression<Collection<Match<Either<SymbolInformation, DocumentSymbol>>>>() {
		//Note: filtering is 'fast' so is done synchronously
		{
			dependsOn(searchBox);
			dependsOn(unfilteredSymbols);
		}
		
		@Override
		protected Collection<Match<Either<SymbolInformation, DocumentSymbol>>> compute() {
			String query = searchBox.getValue();
			if (!StringUtil.hasText(query)) {
				query = "";
			}
			query = query.toLowerCase();
			List<Match<Either<SymbolInformation, DocumentSymbol>>> matches = new ArrayList<>();
			for (Either<SymbolInformation, DocumentSymbol> symbol : unfilteredSymbols.getValues()) {
				String name = null;
				if (symbol.isLeft()) {
					name = symbol.getLeft().getName().toLowerCase();
				}
				else if (symbol.isRight()) {
					name = symbol.getRight().getName().toLowerCase();
				}
				
				double score = FuzzyMatcher.matchScore(query, name);
				if (score!=0.0) {
					matches.add(new Match<Either<SymbolInformation, DocumentSymbol>>(score, query, symbol));
				}
			}
			Collections.sort(matches, MATCH_COMPARATOR);
			return ImmutableList.copyOf(matches);
		}
	};
	
	private String keyBindings;
	private OKHandler okHandler = DEFAULT_OK_HANDLER;

	private FavouritesPreference favourites = null;

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
			unfilteredSymbols.addListener((e, v) -> debug("raw = "+summary(filteredSymbols.getValue())));
			filteredSymbols.addListener((e, v) -> debug("filtered = "+summary(filteredSymbols.getValue())));
		}
	}
	
	public GotoSymbolDialogModel setFavourites(FavouritesPreference favourites) {
		this.favourites = favourites;
		return this;
	}
	
	public FavouritesPreference getFavourites() {
		return favourites;
	}

	private List<String> summary(Collection<Match<Either<SymbolInformation, DocumentSymbol>>> collection) {
		return collection.stream().map(match -> match.value.isLeft() ? match.value.getLeft().getName() : match.value.getRight().getName()).collect(Collectors.toList());
	}

	public LiveExpression<Collection<Match<Either<SymbolInformation, DocumentSymbol>>>> getSymbols() {
		return filteredSymbols;
	}

	public LiveVariable<String> getSearchBox() {
		return searchBox;
	}
	
	public String getSearchBoxHintMessage() {
		return SEARCH_BOX_HINT_MESSAGE;
	}

	public LiveExpression<HighlightedText> getStatus() {
		return status;
	}
	
	public synchronized void toggleSymbolsProvider() {
		currentSymbolsProviderIndex = (currentSymbolsProviderIndex+1)%symbolsProviders.length;
		currentSymbolsProvider.setValue(symbolsProviders[currentSymbolsProviderIndex]);
	}
	
	public SymbolsProvider[] getSymbolsProviders() {
		return symbolsProviders;
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

	public boolean fromFileProvider(SymbolInformation symbolInformation) {
		SymbolsProvider sp = currentSymbolsProvider.getValue();
		if (sp!=null) {
			return sp.fromFile(symbolInformation);
		}
		return false;
	}
}
