package com.wellsfargo.lendingplatform.web.config;

import java.util.List;

public class TestObjectWithList {
    private String[] stringList;
    private Color[] colorList;
    private List<Color> list;

    public String[] getStringList() {
        return stringList;
    }

    public void setStringList(String[] stringList) {
        this.stringList = stringList;
    }

	public Color[] getColorList() {
		return colorList;
	}

	public void setColorList(Color[] colorList) {
		this.colorList = colorList;
	}

	public List<Color> getList() {
		return list;
	}

	public void setList(List<Color> list) {
		this.list = list;
	}
}