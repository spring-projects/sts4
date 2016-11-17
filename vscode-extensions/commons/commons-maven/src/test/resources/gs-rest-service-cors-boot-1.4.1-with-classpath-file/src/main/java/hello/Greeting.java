package hello;

/**
 * Comment for Greeting class 
 */
public class Greeting {

    /**
     * Comment for id field
     */
    private final long id;
    private final String content;

    public Greeting() {
        this.id = -1;
        this.content = "";
    }

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    /**
     * Comment for getId()
     */
    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
    
    /**
     * Comment for inner class
     */
    private static class TestInnerClass {
    	
    	/**
    	 * Comment for inner field
    	 */
    	int innerField;
    	
    	/**
    	 * Comment for method inside nested class
    	 */
    	public int getInnerField() {
    		return innerField;
    	}
    	
    }
}
