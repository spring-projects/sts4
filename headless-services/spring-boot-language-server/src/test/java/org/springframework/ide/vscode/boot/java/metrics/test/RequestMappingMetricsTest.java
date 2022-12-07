/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.metrics.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.livehover.v2.RequestMappingMetrics;

public class RequestMappingMetricsTest {

    @Test
    void testParser1() throws Exception {
        RequestMappingMetrics mappingMetrics = RequestMappingMetrics.parse("{\"name\":\"http.server.requests\",\"description\":null,\"baseUnit\":\"seconds\",\"measurements\":[{\"statistic\":\"COUNT\",\"value\":1.0},{\"statistic\":\"TOTAL_TIME\",\"value\":0.03465965},{\"statistic\":\"MAX\",\"value\":0.47461985}],\"availableTags\":[{\"tag\":\"exception\",\"values\":[\"None\"]},{\"tag\":\"outcome\",\"values\":[\"SUCCESS\"]},{\"tag\":\"status\",\"values\":[\"200\"]}]}");
        assertEquals(TimeUnit.SECONDS, mappingMetrics.getTimeUnit());
        assertEquals(1, mappingMetrics.getCallsCount());
        assertEquals(0.47461985, mappingMetrics.getMaxTime());
        assertEquals(0.03465965, mappingMetrics.getTotalTime());
    }

}
