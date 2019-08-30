package org.springframework.ide.si.view;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.ide.si.view.json.SpringIntegrationGraph;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

public class MockGraphData implements GraphDataProvider {
	
	public static String data = "{\n" + 
			"contentDescriptor: {\n" + 
			"providerVersion: \"5.1.6.RELEASE\",\n" + 
			"providerFormatVersion: 1,\n" + 
			"provider: \"spring-integration\"\n" + 
			"},\n" + 
			"nodes: [\n" + 
			"{\n" + 
			"nodeId: 1,\n" + 
			"name: \"news\",\n" + 
			"stats: {\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"sendCount: 20,\n" + 
			"sendErrorCount: 0,\n" + 
			"timeSinceLastSend: 3017590.1150589883,\n" + 
			"meanSendRate: 0.006416917317469066,\n" + 
			"meanErrorRate: 0,\n" + 
			"meanErrorRatio: 0,\n" + 
			"meanSendDuration: 0.6229622172698512,\n" + 
			"minSendDuration: 0.36042,\n" + 
			"maxSendDuration: 5.398724,\n" + 
			"standardDeviationSendDuration: 0.6248008418287394,\n" + 
			"sendDuration: {\n" + 
			"count: 20,\n" + 
			"min: 0.36042,\n" + 
			"max: 5.398724,\n" + 
			"mean: 0.6229622172698512,\n" + 
			"standardDeviation: 0.6248008418287394,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"sendRate: {\n" + 
			"count: 20,\n" + 
			"min: 4.519030865997076,\n" + 
			"max: 5.000686829984188,\n" + 
			"mean: 0.006416915226255189,\n" + 
			"standardDeviation: 0.003059182339655441,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"errorRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"}\n" + 
			"},\n" + 
			"componentType: \"channel\",\n" + 
			"properties: { }\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 2,\n" + 
			"name: \"file\",\n" + 
			"stats: {\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"sendCount: 20,\n" + 
			"sendErrorCount: 0,\n" + 
			"timeSinceLastSend: 3017591.1391609907,\n" + 
			"meanSendRate: 0.006416922929073101,\n" + 
			"meanErrorRate: 0,\n" + 
			"meanErrorRatio: 0,\n" + 
			"meanSendDuration: 0.35330047484316307,\n" + 
			"minSendDuration: 0.208864,\n" + 
			"maxSendDuration: 2.849803,\n" + 
			"standardDeviationSendDuration: 0.33755534347541105,\n" + 
			"sendDuration: {\n" + 
			"count: 20,\n" + 
			"min: 0.208864,\n" + 
			"max: 2.849803,\n" + 
			"mean: 0.35330047484316307,\n" + 
			"standardDeviation: 0.33755534347541105,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"sendRate: {\n" + 
			"count: 20,\n" + 
			"min: 4.516921804994345,\n" + 
			"max: 5.000899210005999,\n" + 
			"mean: 0.00641692253069279,\n" + 
			"standardDeviation: 0.003073929309381288,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"errorRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"}\n" + 
			"},\n" + 
			"componentType: \"channel\",\n" + 
			"properties: { }\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 3,\n" + 
			"name: \"nullChannel\",\n" + 
			"stats: {\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"sendCount: 0,\n" + 
			"sendErrorCount: 0,\n" + 
			"timeSinceLastSend: 0,\n" + 
			"meanSendRate: 0,\n" + 
			"meanErrorRate: 0,\n" + 
			"meanErrorRatio: 0,\n" + 
			"meanSendDuration: 0,\n" + 
			"minSendDuration: 0,\n" + 
			"maxSendDuration: 0,\n" + 
			"standardDeviationSendDuration: 0,\n" + 
			"sendDuration: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"},\n" + 
			"sendRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"},\n" + 
			"errorRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"}\n" + 
			"},\n" + 
			"componentType: \"null-channel\",\n" + 
			"properties: { }\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 4,\n" + 
			"name: \"errorChannel\",\n" + 
			"stats: {\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"sendCount: 0,\n" + 
			"sendErrorCount: 0,\n" + 
			"timeSinceLastSend: 0,\n" + 
			"meanSendRate: 0,\n" + 
			"meanErrorRate: 0,\n" + 
			"meanErrorRatio: 0,\n" + 
			"meanSendDuration: 0,\n" + 
			"minSendDuration: 0,\n" + 
			"maxSendDuration: 0,\n" + 
			"standardDeviationSendDuration: 0,\n" + 
			"sendDuration: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"},\n" + 
			"sendRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"},\n" + 
			"errorRate: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"}\n" + 
			"},\n" + 
			"componentType: \"publish-subscribe-channel\",\n" + 
			"properties: { }\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 5,\n" + 
			"name: \"news.adapter\",\n" + 
			"stats: {\n" + 
			"messageCount: 20\n" + 
			"},\n" + 
			"componentType: \"feed:inbound-channel-adapter\",\n" + 
			"properties: { },\n" + 
			"output: \"news\",\n" + 
			"errors: null\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 6,\n" + 
			"name: \"org.springframework.integration.config.ConsumerEndpointFactoryBean#0\",\n" + 
			"stats: {\n" + 
			"activeCount: 0,\n" + 
			"duration: {\n" + 
			"count: 20,\n" + 
			"min: 0.346117,\n" + 
			"max: 4.516881,\n" + 
			"mean: 0.585700683679285,\n" + 
			"standardDeviation: 0.5242315172537475,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"maxDuration: 4.516881,\n" + 
			"minDuration: 0.346117,\n" + 
			"meanDuration: 0.585700683679285,\n" + 
			"errorCount: 0,\n" + 
			"handleCount: 20,\n" + 
			"standardDeviationDuration: 0.5242315172537475\n" + 
			"},\n" + 
			"componentType: \"transformer\",\n" + 
			"properties: {\n" + 
			"expression: \"payload.title + ' @ ' + payload.link + ' '\"\n" + 
			"},\n" + 
			"output: \"file\",\n" + 
			"input: \"news\"\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 7,\n" + 
			"name: \"file.adapter\",\n" + 
			"stats: {\n" + 
			"activeCount: 0,\n" + 
			"duration: {\n" + 
			"count: 20,\n" + 
			"min: 0.192626,\n" + 
			"max: 2.602112,\n" + 
			"mean: 0.3295736390049931,\n" + 
			"standardDeviation: 0.31151136752438835,\n" + 
			"countLong: 20\n" + 
			"},\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"maxDuration: 2.602112,\n" + 
			"minDuration: 0.192626,\n" + 
			"meanDuration: 0.3295736390049931,\n" + 
			"errorCount: 0,\n" + 
			"handleCount: 20,\n" + 
			"standardDeviationDuration: 0.31151136752438835\n" + 
			"},\n" + 
			"componentType: \"message-handler\",\n" + 
			"properties: { },\n" + 
			"output: null,\n" + 
			"input: \"file\"\n" + 
			"},\n" + 
			"{\n" + 
			"nodeId: 8,\n" + 
			"name: \"_org.springframework.integration.errorLogger\",\n" + 
			"stats: {\n" + 
			"activeCount: 0,\n" + 
			"duration: {\n" + 
			"count: 0,\n" + 
			"min: 0,\n" + 
			"max: 0,\n" + 
			"mean: 0,\n" + 
			"standardDeviation: 0,\n" + 
			"countLong: 0\n" + 
			"},\n" + 
			"countsEnabled: true,\n" + 
			"statsEnabled: true,\n" + 
			"loggingEnabled: true,\n" + 
			"maxDuration: 0,\n" + 
			"minDuration: 0,\n" + 
			"meanDuration: 0,\n" + 
			"errorCount: 0,\n" + 
			"handleCount: 0,\n" + 
			"standardDeviationDuration: 0\n" + 
			"},\n" + 
			"componentType: \"logging-channel-adapter\",\n" + 
			"properties: { },\n" + 
			"output: null,\n" + 
			"input: \"errorChannel\"\n" + 
			"}\n" + 
			"],\n" + 
			"links: [\n" + 
			"{\n" + 
			"from: 5,\n" + 
			"to: 1,\n" + 
			"type: \"output\"\n" + 
			"},\n" + 
			"{\n" + 
			"from: 1,\n" + 
			"to: 6,\n" + 
			"type: \"input\"\n" + 
			"},\n" + 
			"{\n" + 
			"from: 6,\n" + 
			"to: 2,\n" + 
			"type: \"output\"\n" + 
			"},\n" + 
			"{\n" + 
			"from: 2,\n" + 
			"to: 7,\n" + 
			"type: \"input\"\n" + 
			"},\n" + 
			"{\n" + 
			"from: 4,\n" + 
			"to: 8,\n" + 
			"type: \"input\"\n" + 
			"}\n" + 
			"]\n" + 
			"}";

	@Override
	public SpringIntegrationGraph getGraph() throws IOException {
//		Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
//		JsonObject json = gson.fromJson(data, JsonObject.class);
//		System.out.println(gson.toJson(json));
		
		String data = IOUtils.toString(getClass().getResource("/static/sample.json"));
		return new Gson().fromJson(data, SpringIntegrationGraph.class);
	}
}
