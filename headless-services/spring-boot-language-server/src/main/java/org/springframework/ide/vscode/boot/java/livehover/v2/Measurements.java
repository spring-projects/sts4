package org.springframework.ide.vscode.boot.java.livehover.v2;

public class Measurements {
	
	private String statistic;
	private Long value;
	
	public String getStatistic() {
		return statistic;
	}


	public Long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Measurements [statistic=" + statistic + ", value=" + value + "]";
	}
	
}
