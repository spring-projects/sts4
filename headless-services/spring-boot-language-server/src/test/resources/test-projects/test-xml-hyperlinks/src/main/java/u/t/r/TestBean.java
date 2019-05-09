package u.t.r;

public class TestBean extends SuperTestBean {
	
	private int age = 5;
	
	private TestBean spouse;

	private SimpleObj simple;
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public TestBean getSpouse() {
		return spouse;
	}

	public void setSpouse(TestBean spouse) {
		this.spouse = spouse;
	}

	public SimpleObj getSimple() {
		return simple;
	}

	public void setSimple(SimpleObj simple) {
		this.simple = simple;
	}

}
