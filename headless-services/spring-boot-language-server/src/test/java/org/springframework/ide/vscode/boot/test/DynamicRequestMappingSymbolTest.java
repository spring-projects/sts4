package org.springframework.ide.vscode.boot.test;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
@Ignore
public class DynamicRequestMappingSymbolTest {

	@Autowired LanguageServerHarness harness;
//	@Autowired MockRunningAppProvider mockApps;
//
//	@Test
//	public void runningAppProviderRequestMappingSymbol() throws Exception {
//		mockApps.builder()
//		.isSpringBootApp(true)
//		.port("1111")
//		.processId("22022")
//		.host("cfapps.io")
//		.urlScheme("https")
//		.processName("test-request-mapping-live-hover")
//		.requestMappings(ImmutableList.of(
//				new MockRequestMapping()
//				.className("example.HelloWorldController")
//				.methodName("sayHello")
//				.methodParams("java.lang.String")
//				.paths("/blah")
//		))
//		.build();
//		harness.assertWorkspaceSymbols("//",
//				"https://cfapps.io:1111/blah"
//		);
//	}
//
//	@Test
//	public void noPaths() throws Exception {
//		mockApps.builder()
//		.isSpringBootApp(true)
//		.port("1111")
//		.processId("22022")
//		.host("cfapps.io")
//		.urlScheme("https")
//		.processName("test-request-mapping-live-hover")
//		.requestMappings(ImmutableList.of(
//				new MockRequestMapping()
//				.className("example.HelloWorldController")
//				.methodName("sayHello")
//				.methodParams("java.lang.String")
//				.paths()
//		))
//		.build();
//		harness.assertWorkspaceSymbols("//",
//				"https://cfapps.io:1111/"
//		);
//	}
//
//	@Test
//	public void multiplePaths() throws Exception {
//		mockApps.builder()
//		.isSpringBootApp(true)
//		.port("80")
//		.processId("22022")
//		.host("localhost")
//		.urlScheme("http")
//		.processName("test-request-mapping-live-hover")
//		.requestMappings(ImmutableList.of(
//				new MockRequestMapping()
//				.className("example.HelloWorldController")
//				.methodName("sayHello")
//				.methodParams("java.lang.String")
//				.paths("foo", "/bar")
//		))
//		.build();
//		harness.assertWorkspaceSymbols("//",
//				"http://localhost/foo",
//				"http://localhost/bar"
//		);
//	}

}
