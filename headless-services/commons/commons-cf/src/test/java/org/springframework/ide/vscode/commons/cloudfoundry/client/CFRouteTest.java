package org.springframework.ide.vscode.commons.cloudfoundry.client;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class CFRouteTest {

	public static final List<String> SPRING_CLOUD_DOMAINS = Arrays.<String>asList("springsource.org", "spring.io",
			"myowndomain.spring.io", "tcp.spring.io", "spring.framework");


	@Test
	public void test_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp.spring.io", route.getRoute());
	}

	@Test
	public void test_domain_only() throws Exception {
		CFRoute route = CFRoute.builder().from("spring.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("spring.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_match() throws Exception {
		CFRoute route = CFRoute.builder().from("myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("myowndomain.spring.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myowndomain.spring.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting() throws Exception {
		// For domains that do not exist, the first segment is assumed to be the "host"
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("doesnotexist.io", route.getDomain());
		Assertions.assertEquals("app",route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("app.doesnotexist.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting_path() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io/withpath", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("doesnotexist.io", route.getDomain());
		Assertions.assertEquals("app",route.getHost());
		Assertions.assertEquals("/withpath",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("app.doesnotexist.io/withpath", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting_path_port() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io:60100/withpath", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("doesnotexist.io", route.getDomain());
		Assertions.assertEquals("app",route.getHost());
		Assertions.assertEquals("/withpath",route.getPath());
		Assertions.assertEquals(60100, route.getPort());
		Assertions.assertEquals("app.doesnotexist.io:60100/withpath", route.getRoute());
	}

	@Test
	public void test_longer_domain_match_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("myowndomain.spring.io", route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp.myowndomain.spring.io", route.getRoute());
	}

	@Test
	public void test_domain_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertEquals("/appPath", route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp.spring.io/appPath", route.getRoute());
	}

	@Test
	public void test_domain_host_path_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath/additionalSegment", SPRING_CLOUD_DOMAINS)
				.build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertEquals("/appPath/additionalSegment", route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp.spring.io/appPath/additionalSegment", route.getRoute());
	}

	@Test
	public void test_tcp_port() throws Exception {
		CFRoute route = CFRoute.builder().from("tcp.spring.io:9000", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("tcp.spring.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(9000, route.getPort());
		Assertions.assertEquals("tcp.spring.io:9000", route.getRoute());
	}

	@Test
	public void test_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("justhost/path", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("justhost",route.getHost());
		Assertions.assertEquals("/path",route.getPath());
		Assertions.assertEquals("justhost/path",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_routes() throws Exception {
		// A CFRoute does not validate route values. It can create any CF route even with wrong domains
		// ports, hosts.. This tests that the route builder is parsing an invalid route into different
		// components that some other external mechanism (like the CF Java client) can the use to validate

		CFRoute route = CFRoute.builder().from("", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(null, SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(".", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(".",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("justhost", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("justhost",route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("justhost",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("justhost.", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("justhost",route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("justhost.",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(".justdomain", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("justdomain",route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(".justdomain",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("..justdomain", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals(".justdomain",route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("..justdomain",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());


		route = CFRoute.builder().from("/justpath/morepath", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/justpath/morepath",route.getPath());
		Assertions.assertEquals("/justpath/morepath",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("/", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/",route.getPath());
		Assertions.assertEquals("/",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_incorrect_ports() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io:notAn1nt3g3r", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("myapp.spring.io:notAn1nt3g3r",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		// Test parsing around the first encountered ':'
		route = CFRoute.builder().from("https://myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("https://myapp.spring.io",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("tcp.spring.io:8000:9000", SPRING_CLOUD_DOMAINS).build();
		// Only one ':' is allowed. it should not be able to parse a port if more than ':' is encountered
		Assertions.assertEquals("tcp.spring.io:8000:9000",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());


		route = CFRoute.builder().from("myapp.spring.io:8000/", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("/",route.getPath());
		Assertions.assertEquals("myapp.spring.io:8000/",route.getRoute());
		Assertions.assertEquals(8000, route.getPort());
	}

	@Test
	public void test_incorrect_paths() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io//path", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("//path",route.getPath());
		Assertions.assertEquals("myapp.spring.io//path",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("myapp.spring.io/", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("/",route.getPath());
		Assertions.assertEquals("myapp.spring.io/",route.getRoute());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void parse_null_domain() throws Exception {
		String domain = CFRouteBuilder.findDomain("", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain(null, SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("cfapps.", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("...", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps..", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps..", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps.", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);
	}

	@Test
	public void parse_valid_domain() throws Exception {

		// These exist
		String domain = CFRouteBuilder.findDomain("spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain(".spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("..spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("myowndomain.spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertEquals("myowndomain.spring.io", domain);
	}

	@Test
	public void parse_invalid_domain() throws Exception {

		// These variations of existing domains don't exist
		String domain = CFRouteBuilder.findDomain("spring.io.", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("spring.cfapps.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("spring.io.cfapps", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("unknown", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);

		domain = CFRouteBuilder.findDomain("unknown.domain.io", SPRING_CLOUD_DOMAINS);
		Assertions.assertNull(domain);
	}

	@Test
	public void bug_142279275_parse_hostAndPathSameName() throws Exception {

		// Fixes Pivotal Tracker item 142279275
		CFRoute route = CFRoute.builder().from("hello-user.myowndomain.spring.io/hello", SPRING_CLOUD_DOMAINS).build();
		Assertions.assertEquals("hello-user", route.getHost());
		Assertions.assertEquals("myowndomain.spring.io", route.getDomain());
		Assertions.assertEquals("/hello", route.getPath());
        Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
        Assertions.assertEquals("hello-user.myowndomain.spring.io/hello", route.getRoute());
	}

	@Test
	public void build_route_value_empty() throws Exception {

		String val = CFRouteBuilder.buildRouteVal(null, null, null, CFRoute.NO_PORT);
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, val);

		val = CFRouteBuilder.buildRouteVal("", "", "", CFRoute.NO_PORT);
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, val);

	}

	@Test
	public void build_route_value() throws Exception {

		String val = CFRouteBuilder.buildRouteVal("appHost", null, null, CFRoute.NO_PORT);
		Assertions.assertEquals("appHost", val);

		val = CFRouteBuilder.buildRouteVal(null, "cfapps.io", "", CFRoute.NO_PORT);
		Assertions.assertEquals("cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "", CFRoute.NO_PORT);
		Assertions.assertEquals("appHost.cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", CFRoute.NO_PORT);
		Assertions.assertEquals("/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", 8000);
		Assertions.assertEquals(":8000/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, null, 60101);
		Assertions.assertEquals(":60101", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", CFRoute.NO_PORT);
		Assertions.assertEquals("appHost.cfapps.io/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", 60101);
		Assertions.assertEquals("appHost.cfapps.io:60101/path/to/app", val);
	}

	@Test
	public void test_build_route_from_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("spring.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_nonexisting_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("not.exist.io").build();
		Assertions.assertEquals("not.exist.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("not.exist.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_null_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain(null).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_empty_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("").build();
		Assertions.assertEquals("",route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
	}

	@Test
	public void test_build_route_from_host() throws Exception {
		CFRoute route = CFRoute.builder().host("myapp").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp", route.getRoute());
	}

	@Test
	public void test_build_route_from_null_host() throws Exception {
		CFRoute route = CFRoute.builder().host(null).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getPath());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_empty_host() throws Exception {
		CFRoute route = CFRoute.builder().host("").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals("",route.getHost());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").host("myapp").build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp.spring.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/path").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/path",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("/path", route.getRoute());
	}

	@Test
	public void test_build_route_from_path_2() throws Exception {
		CFRoute route = CFRoute.builder().path("/path/additional").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/path/additional",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("/path/additional", route.getRoute());
	}

	@Test
	public void test_build_route_from_path_3() throws Exception {
		CFRoute route = CFRoute.builder().path("/path/").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/path/",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("/path/", route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").domain("spring.io").build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/mypath",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("spring.io/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").host("myapp").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("/mypath",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path_samename() throws Exception {
		CFRoute route = CFRoute.builder().path("/myapp").host("myapp").build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("/myapp",route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals("myapp/myapp", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path_port() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").host("myapp").port(8000).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertEquals("myapp",route.getHost());
		Assertions.assertEquals("/mypath",route.getPath());
		Assertions.assertEquals(8000, route.getPort());
		Assertions.assertEquals("myapp:8000/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_path_port() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").domain("spring.io").port(8000).build();
		Assertions.assertEquals("spring.io", route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertEquals("/mypath",route.getPath());
		Assertions.assertEquals(8000, route.getPort());
		Assertions.assertEquals("spring.io:8000/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_port() throws Exception {
		CFRoute route = CFRoute.builder().port(8000).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(8000, route.getPort());
		Assertions.assertEquals(":8000", route.getRoute());
	}


	@Test
	public void test_build_route_from_no_port() throws Exception {
		CFRoute route = CFRoute.builder().port(CFRoute.NO_PORT).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_no_port_2() throws Exception {
		CFRoute route = CFRoute.builder().port(-1).build();
		Assertions.assertNull(route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assertions.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_tcp_port_building() throws Exception {
		CFRoute route = CFRoute.builder().domain("tcp.spring.io").port(8000).build();
		Assertions.assertEquals("tcp.spring.io",route.getDomain());
		Assertions.assertNull(route.getHost());
		Assertions.assertNull(route.getPath());
		Assertions.assertEquals(8000, route.getPort());
		Assertions.assertEquals("tcp.spring.io:8000", route.getRoute());
	}


	@Test
	public void test_complete() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").host("myapp").path("/mypath/additional").port(8000).build();
		Assertions.assertEquals("spring.io",route.getDomain());
		Assertions.assertEquals("myapp", route.getHost());
		Assertions.assertEquals("/mypath/additional", route.getPath());
		Assertions.assertEquals(8000, route.getPort());
		Assertions.assertEquals("myapp.spring.io:8000/mypath/additional", route.getRoute());
	}
}
