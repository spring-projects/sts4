package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my")
public class MyProperties {
	
	public static class Screen {
		private Color foreground;
		private Color background;
		public Color getForeground() {
			return foreground;
		}
		public void setForeground(Color foreground) {
			this.foreground = foreground;
		}
		public Color getBackground() {
			return background;
		}
		public void setBackground(Color background) {
			this.background = background;
		}
	}
	
	private Screen screen;
	private Screen[] screenArray;
	private List<Screen> screenList;
	private Map<Color, Screen> screenMap;

	public Screen getScreen() {
		return screen;
	}

	public void setScreen(Screen screen) {
		this.screen = screen;
	}

	public Screen[] getScreenArray() {
		return screenArray;
	}

	public void setScreenArray(Screen[] screenArray) {
		this.screenArray = screenArray;
	}

	public List<Screen> getScreenList() {
		return screenList;
	}

	public void setScreenList(List<Screen> screenList) {
		this.screenList = screenList;
	}

	public Map<Color, Screen> getScreenMap() {
		return screenMap;
	}

	public void setScreenMap(Map<Color, Screen> screenMap) {
		this.screenMap = screenMap;
	}
	
}
