package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public interface RequestMappingMetrics {
	
	long getCallsCount();
	
	double getTotalTime();
	
	double getMaxTime();
	
	TimeUnit getTimeUnit();

	static RequestMappingMetrics parse(String metricsData) {
		final JSONObject obj = new JSONObject(metricsData);
		final TimeUnit timeUnit = TimeUnit.valueOf(obj.getString("baseUnit").toUpperCase());
		final JSONArray measurements = obj.getJSONArray("measurements");
		return new RequestMappingMetrics() {
			
			@Override
			public double getTotalTime() {
				Double d = findStatistic(measurements, "TOTAL_TIME");
				return d == null ? 0 : d.doubleValue();
			}
			
			@Override
			public double getMaxTime() {
				Double d = findStatistic(measurements, "MAX");
				return d == null ? 0 : d.doubleValue();
			}
			

			@Override
			public long getCallsCount() {
				Double d = findStatistic(measurements, "COUNT");
				return d == null ? 0 : d.longValue();
			}

			@Override
			public TimeUnit getTimeUnit() {
				return timeUnit == null ? TimeUnit.SECONDS : timeUnit;
			}
			
			@SuppressWarnings("unchecked")
			private <T> T findStatistic(JSONArray measurements, String statistic) {
				for (int i = 0; i < measurements.length(); i++) {
					JSONObject entry = measurements.getJSONObject(i);
					if (statistic.equals(entry.getString("statistic"))) {
						return (T) entry.get("value");
					}
				}
				return null;
			}
		};
	}

}
