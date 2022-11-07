/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

public class SpringProcessConnectorOverHttp implements SpringProcessConnector {

	private final ProcessType processType;
	private final String processKey;
	private final String actuatorUrl;
	private final String urlScheme;
	private final String port;
	private final String projectName;
	
	// not final, might be updated with data from JMX process, if not initially set
	private String processID;
	private String processName;
	private String host;
	
	private HttpActuatorConnection actuatorConnection;
	
	public SpringProcessConnectorOverHttp(ProcessType processType, String processKey, String actuatorUrl,
			String urlScheme, String processID, String processName, String projectName, String host, String port) {
		
		this.processType = processType;
		this.processKey = processKey;

		this.actuatorUrl = actuatorUrl;
		this.urlScheme = urlScheme;
		this.processID = processID;
		this.processName = processName;
		this.projectName = projectName;
		this.host = host;
		this.port = port;
	}


	@Override
	public ProcessType getProcessType() {
		return processType;
	}

	@Override
	public String getProcessKey() {
		return processKey;
	}
	
	@Override
	public void connect() throws Exception {
		actuatorConnection = new HttpActuatorConnection(actuatorUrl);
	}

	@Override
	public SpringProcessLiveData refresh(SpringProcessLiveData currentData) throws Exception {
		if (actuatorConnection != null) {
			SpringProcessLiveData liveData = new SpringProcessLiveDataExtractorOverHttp().retrieveLiveData(getProcessType(), actuatorConnection, processID, processName, urlScheme, host, null, port, currentData);
			
			if (this.processID == null) {
				this.processID = liveData.getProcessID();
			}

			if (this.processName == null) {
				this.processName = liveData.getProcessName();
			}

			if (liveData != null && liveData.getBeans() != null && !liveData.getBeans().isEmpty()) {
				return liveData;
			}
		}
		
		throw new Exception("no live data received, lets try again");
	}

	@Override
	public void disconnect() throws Exception {
		actuatorConnection = null;
	}

	@Override
	public void addConnectorChangeListener(SpringProcessConnectionChangeListener listener) {
		// Useless in the case of Http connection
	}

	@Override
	public void removeConnectorChangeListener(SpringProcessConnectionChangeListener listener) {
		// Useless in the case of Http connection
	}
	
	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getProcessId() {
		return processID;
	}

	@Override
	public String getProcessName() {
		return processName;
	}

	@Override
	public String toString() {
		return "SpringProcessConnectorOverHttp [actuatorURL=" + actuatorUrl + "]";
	}

	@Override
	public SpringProcessGcPausesMetricsLiveData refreshGcPausesMetrics(SpringProcessLiveData current, String metricName, String tags)
			throws Exception {
	    if (actuatorConnection != null) {
	        SpringProcessGcPausesMetricsLiveData liveData = new SpringProcessLiveDataExtractorOverHttp().retrieveLiveGcPausesMetricsData(getProcessType(), actuatorConnection, processID, processName, current, metricName, tags);
            
            if (this.processID == null) {
                this.processID = liveData.getProcessID();
            }

            if (this.processName == null) {
                this.processName = liveData.getProcessName();
            }

            if (liveData != null && liveData.getGcPausesMetrics() != null && liveData.getGcPausesMetrics().length > 0) {
                return liveData;
            }
        }
        
        throw new Exception("no live gc pauses metric data received, lets try again");
	}

	@Override
	public SpringProcessMemoryMetricsLiveData refreshMemoryMetrics(SpringProcessLiveData current, String metricName, String tags)
			throws Exception {
	    if (actuatorConnection != null) {
	        SpringProcessMemoryMetricsLiveData liveData = new SpringProcessLiveDataExtractorOverHttp().retrieveLiveMemoryMetricsData(getProcessType(), actuatorConnection, processID, processName, current, metricName, tags);
            
            if (this.processID == null) {
                this.processID = liveData.getProcessID();
            }

            if (this.processName == null) {
                this.processName = liveData.getProcessName();
            }

            if (liveData != null && liveData.getMemoryMetrics() != null && liveData.getMemoryMetrics().length > 0) {
                return liveData;
            }
        }
        
        throw new Exception("no live memory metrics data received, lets try again");
	}

	
}
