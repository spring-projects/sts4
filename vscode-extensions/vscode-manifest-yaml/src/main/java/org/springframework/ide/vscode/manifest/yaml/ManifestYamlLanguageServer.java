package org.springframework.ide.vscode.manifest.yaml;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargets;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.completion.SchemaBasedYamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlAssistContextProvider;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngine;
import org.springframework.ide.vscode.commons.yaml.hover.YamlHoverInfoProvider;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaBasedReconcileEngine;
import org.springframework.ide.vscode.commons.yaml.schema.YValueHint;
import org.springframework.ide.vscode.commons.yaml.schema.YamlSchema;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

public class ManifestYamlLanguageServer extends SimpleLanguageServer {

	private static final Provider<Collection<YValueHint>> NO_PROVIDER = () -> ImmutableList.of();
	private static Logger logger = Logger.getLogger(ManifestYamlLanguageServer.class.getName());
	
	private Yaml yaml = new Yaml();
	private YamlSchema schema;
	private CFTargets cfTargets;

	
	public ManifestYamlLanguageServer() {
		SimpleTextDocumentService documents = getTextDocumentService();
		
		YamlASTProvider parser = new YamlParser(yaml);
		
		CFTargets cfTargets = getCFTargets();
		
		Provider<Collection<YValueHint>> buildPacksProvider = getBuildpacksProvider(cfTargets);
		Provider<Collection<YValueHint>> servicesProvider = getServicesProvider(cfTargets);

		schema = new ManifestYmlSchema(buildPacksProvider, servicesProvider);

		YamlStructureProvider structureProvider = YamlStructureProvider.DEFAULT;
		YamlAssistContextProvider contextProvider = new SchemaBasedYamlAssistContextProvider(schema);
		YamlCompletionEngine yamlCompletionEngine = new YamlCompletionEngine(structureProvider, contextProvider);
		VscodeCompletionEngine completionEngine = new VscodeCompletionEngineAdapter(this, yamlCompletionEngine);
		HoverInfoProvider infoProvider = new YamlHoverInfoProvider(parser, structureProvider, contextProvider);
		VscodeHoverEngine hoverEngine = new VscodeHoverEngineAdapter(this, infoProvider);
		IReconcileEngine engine = new YamlSchemaBasedReconcileEngine(parser, schema);

//		SimpleWorkspaceService workspace = getWorkspaceService();
		documents.onDidChangeContent(params -> {
			TextDocument doc = params.getDocument();
			validateWith(doc, engine);
		});
		
//		workspace.onDidChangeConfiguraton(settings -> {
//			System.out.println("Config changed: "+params);
//			Integer val = settings.getInt("languageServerExample", "maxNumberOfProblems");
//			if (val!=null) {
//				maxProblems = ((Number) val).intValue();
//				for (TextDocument doc : documents.getAll()) {
//					validateDocument(documents, doc);
//				}
//			}
//		});
		
		documents.onCompletion(completionEngine::getCompletions);
		documents.onCompletionResolve(completionEngine::resolveCompletion);
		documents.onHover(hoverEngine ::getHover);
	}
	
	private CFTargets getCFTargets() {
		// TODO: probably shouldn't be cached as targets can change during a language server session
		if (cfTargets == null) {
			cfTargets = CFTargets.createDefaultV2Targets();
		}
		return cfTargets;
	}

	private Provider<Collection<YValueHint>> getBuildpacksProvider(CFTargets targets) {

		try {
			if (targets != null) {
				List<CFTarget> cfTargets = targets.getTargets();
				if (cfTargets != null && !cfTargets.isEmpty()) {;
					return new ManifestYamlCFBuildpacksProvider(cfTargets);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		return NO_PROVIDER;
	}
	
	private Provider<Collection<YValueHint>> getServicesProvider(CFTargets targets) {

		try {
			if (targets != null) {
				List<CFTarget> cfTargets = targets.getTargets();
				if (cfTargets != null && !cfTargets.isEmpty()) {
					return new ManifestYamlCFServicesProvider(cfTargets);
				}
			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		return null;
	}
	
	@Override
	protected ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();
		
		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		c.setHoverProvider(true);
		
		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);
		
		return c;
	}
}
