package org.springframework.ide.vscode.commons.cloudfoundry.client;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CFRouteTest {

	public static final List<String> SPRING_CLOUD_DOMAINS = Arrays.<String>asList("springsource.org", "spring.io",
			"myowndomain.spring.io", "tcp.spring.io", "spring.framework");


	@Test
	public void test_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp.spring.io", route.getRoute());
	}

	@Test
	public void test_domain_only() throws Exception {
		CFRoute route = CFRoute.builder().from("spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("spring.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_match() throws Exception {
		CFRoute route = CFRoute.builder().from("myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myowndomain.spring.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting() throws Exception {
		// For domains that do not exist, the first segment is assumed to be the "host"
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("app.doesnotexist.io", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting_path() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io/withpath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertEquals("/withpath",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("app.doesnotexist.io/withpath", route.getRoute());
	}

	@Test
	public void test_longer_domain_nonexisting_path_port() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io:60100/withpath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertEquals("/withpath",route.getPath());
		Assert.assertEquals(60100, route.getPort());
		Assert.assertEquals("app.doesnotexist.io:60100/withpath", route.getRoute());
	}

	@Test
	public void test_longer_domain_match_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp.myowndomain.spring.io", route.getRoute());
	}

	@Test
	public void test_domain_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals("/appPath", route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp.spring.io/appPath", route.getRoute());
	}

	@Test
	public void test_domain_host_path_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath/additionalSegment", SPRING_CLOUD_DOMAINS)
				.build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals("/appPath/additionalSegment", route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp.spring.io/appPath/additionalSegment", route.getRoute());
	}

	@Test
	public void test_tcp_port() throws Exception {
		CFRoute route = CFRoute.builder().from("tcp.spring.io:9000", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("tcp.spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(9000, route.getPort());
		Assert.assertEquals("tcp.spring.io:9000", route.getRoute());
	}

	@Test
	public void test_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("justhost/path", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("justhost",route.getHost());
		Assert.assertEquals("/path",route.getPath());
		Assert.assertEquals("justhost/path",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_routes() throws Exception {
		// A CFRoute does not validate route values. It can create any CF route even with wrong domains
		// ports, hosts.. This tests that the route builder is parsing an invalid route into different
		// components that some other external mechanism (like the CF Java client) can the use to validate

		CFRoute route = CFRoute.builder().from("", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(null, SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(".", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(".",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("justhost", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("justhost",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("justhost",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("justhost.", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("justhost",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("justhost.",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from(".justdomain", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("justdomain",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(".justdomain",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("..justdomain", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals(".justdomain",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("..justdomain",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());


		route = CFRoute.builder().from("/justpath/morepath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/justpath/morepath",route.getPath());
		Assert.assertEquals("/justpath/morepath",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("/", SPRING_CLOUD_DOMAINS).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/",route.getPath());
		Assert.assertEquals("/",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_incorrect_ports() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io:notAn1nt3g3r", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("myapp.spring.io:notAn1nt3g3r",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		// Test parsing around the first encountered ':'
		route = CFRoute.builder().from("https://myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("https://myapp.spring.io",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("tcp.spring.io:8000:9000", SPRING_CLOUD_DOMAINS).build();
		// Only one ':' is allowed. it should not be able to parse a port if more than ':' is encountered
		Assert.assertEquals("tcp.spring.io:8000:9000",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());


		route = CFRoute.builder().from("myapp.spring.io:8000/", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("/",route.getPath());
		Assert.assertEquals("myapp.spring.io:8000/",route.getRoute());
		Assert.assertEquals(8000, route.getPort());
	}

	@Test
	public void test_incorrect_paths() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io//path", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("//path",route.getPath());
		Assert.assertEquals("myapp.spring.io//path",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());

		route = CFRoute.builder().from("myapp.spring.io/", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("/",route.getPath());
		Assert.assertEquals("myapp.spring.io/",route.getRoute());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void parse_null_domain() throws Exception {
		String domain = CFRouteBuilder.findDomain("", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(null, SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("cfapps.", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("...", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps..", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps..", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps.", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);
	}

	@Test
	public void parse_valid_domain() throws Exception {

		// These exist
		String domain = CFRouteBuilder.findDomain("spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain(".spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("..spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("myowndomain.spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("myowndomain.spring.io", domain);
	}

	@Test
	public void parse_invalid_domain() throws Exception {

		// These variations of existing domains don't exist
		String domain = CFRouteBuilder.findDomain("spring.io.", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("spring.cfapps.io", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("spring.io.cfapps", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("unknown", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("unknown.domain.io", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);
	}

	@Test
	public void bug_142279275_parse_hostAndPathSameName() throws Exception {

		// Fixes Pivotal Tracker item 142279275
		CFRoute route = CFRoute.builder().from("hello-user.myowndomain.spring.io/hello", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("hello-user", route.getHost());
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertEquals("/hello", route.getPath());
        Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
        Assert.assertEquals("hello-user.myowndomain.spring.io/hello", route.getRoute());
	}

	@Test
	public void build_route_value_empty() throws Exception {

		String val = CFRouteBuilder.buildRouteVal(null, null, null, CFRoute.NO_PORT);
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, val);

		val = CFRouteBuilder.buildRouteVal("", "", "", CFRoute.NO_PORT);
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, val);

	}

	@Test
	public void build_route_value() throws Exception {

		String val = CFRouteBuilder.buildRouteVal("appHost", null, null, CFRoute.NO_PORT);
		Assert.assertEquals("appHost", val);

		val = CFRouteBuilder.buildRouteVal(null, "cfapps.io", "", CFRoute.NO_PORT);
		Assert.assertEquals("cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "", CFRoute.NO_PORT);
		Assert.assertEquals("appHost.cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", CFRoute.NO_PORT);
		Assert.assertEquals("/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", 8000);
		Assert.assertEquals(":8000/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, null, 60101);
		Assert.assertEquals(":60101", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", CFRoute.NO_PORT);
		Assert.assertEquals("appHost.cfapps.io/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", 60101);
		Assert.assertEquals("appHost.cfapps.io:60101/path/to/app", val);
	}

	@Test
	public void test_build_route_from_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("spring.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_nonexisting_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("not.exist.io").build();
		Assert.assertEquals("not.exist.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("not.exist.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_null_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain(null).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_empty_domain() throws Exception {
		CFRoute route = CFRoute.builder().domain("").build();
		Assert.assertEquals("",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE,route.getRoute());
	}

	@Test
	public void test_build_route_from_host() throws Exception {
		CFRoute route = CFRoute.builder().host("myapp").build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp", route.getRoute());
	}

	@Test
	public void test_build_route_from_null_host() throws Exception {
		CFRoute route = CFRoute.builder().host(null).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getPath());
		Assert.assertNull(route.getHost());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_empty_host() throws Exception {
		CFRoute route = CFRoute.builder().host("").build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getPath());
		Assert.assertEquals("",route.getHost());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").host("myapp").build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp.spring.io", route.getRoute());
	}

	@Test
	public void test_build_route_from_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/path").build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/path",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("/path", route.getRoute());
	}

	@Test
	public void test_build_route_from_path_2() throws Exception {
		CFRoute route = CFRoute.builder().path("/path/additional").build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/path/additional",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("/path/additional", route.getRoute());
	}

	@Test
	public void test_build_route_from_path_3() throws Exception {
		CFRoute route = CFRoute.builder().path("/path/").build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/path/",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("/path/", route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").domain("spring.io").build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/mypath",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("spring.io/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").host("myapp").build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("/mypath",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path_samename() throws Exception {
		CFRoute route = CFRoute.builder().path("/myapp").host("myapp").build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("/myapp",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals("myapp/myapp", route.getRoute());
	}

	@Test
	public void test_build_route_from_host_path_port() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").host("myapp").port(8000).build();
		Assert.assertNull(route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("/mypath",route.getPath());
		Assert.assertEquals(8000, route.getPort());
		Assert.assertEquals("myapp:8000/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_domain_path_port() throws Exception {
		CFRoute route = CFRoute.builder().path("/mypath").domain("spring.io").port(8000).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertEquals("/mypath",route.getPath());
		Assert.assertEquals(8000, route.getPort());
		Assert.assertEquals("spring.io:8000/mypath", route.getRoute());
	}

	@Test
	public void test_build_route_from_port() throws Exception {
		CFRoute route = CFRoute.builder().port(8000).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(8000, route.getPort());
		Assert.assertEquals(":8000", route.getRoute());
	}


	@Test
	public void test_build_route_from_no_port() throws Exception {
		CFRoute route = CFRoute.builder().port(CFRoute.NO_PORT).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_build_route_from_no_port_2() throws Exception {
		CFRoute route = CFRoute.builder().port(-1).build();
		Assert.assertNull(route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
		Assert.assertEquals(CFRoute.EMPTY_ROUTE, route.getRoute());
	}

	@Test
	public void test_tcp_port_building() throws Exception {
		CFRoute route = CFRoute.builder().domain("tcp.spring.io").port(8000).build();
		Assert.assertEquals("tcp.spring.io",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(8000, route.getPort());
		Assert.assertEquals("tcp.spring.io:8000", route.getRoute());
	}


	@Test
	public void test_complete() throws Exception {
		CFRoute route = CFRoute.builder().domain("spring.io").host("myapp").path("/mypath/additional").port(8000).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals("/mypath/additional", route.getPath());
		Assert.assertEquals(8000, route.getPort());
		Assert.assertEquals("myapp.spring.io:8000/mypath/additional", route.getRoute());
	}
}
