package demo;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("foo")
public class FooProperties {

	/**
	 * Pojo
	 */
	private ColorData data; 
	 
	//Enum
	private Color color;

	//Map Enum -> Atomic
	private Map<Color,String> colorNames;

	//Map Atomic -> Enum
	private Map<String,Color> nameColors;

	//Map Enum -> Pojo
	private Map<Color, ColorData> colorData;
	
	//List
	private List<String> list;

	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}

	public Map<Color,String> getColorNames() {
		return colorNames;
	}
	public void setColorNames(Map<Color,String> colorNames) {
		this.colorNames = colorNames;
	}
	public Map<String,Color> getNameColors() {
		return nameColors;
	}
	public void setNameColors(Map<String,Color> nameColors) {
		this.nameColors = nameColors;
	}
	public Map<Color, ColorData> getColorData() {
		return colorData;
	}
	public void setColorData(Map<Color, ColorData> colorData) {
		this.colorData = colorData;
	}
	public ColorData getData() {
		return data;
	}
	public void setData(ColorData data) {
		this.data = data;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
}
