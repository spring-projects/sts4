/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

public class Measurements {
	
	private String statistic;
	private Double value;
	
	public String getStatistic() {
		return statistic;
	}


	public Double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Measurements [statistic=" + statistic + ", value=" + value + "]";
	}
	
}
