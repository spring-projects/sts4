package hello;

/**
 * Comment for Greeting class 
 */
public class Greeting {

    /**
     * Comment for id field
     */
    protected final long id;
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
    public class TestInnerClass {
    	
    	/**
    	 * Comment for inner field
    	 */
    	protected int innerField;
    	
    	/**
    	 * Comment for method inside nested class
    	 */
    	public int getInnerField() {
    		return innerField;
    	}
    	
    	/**
    	 * Comment for level 2 nested class 
    	 */
    	public class TestInnerClassLevel2 {
    		
        	/**
        	 * Comment for level 2 inner field
        	 */
    		protected int innerLevel2Field;

        	/**
        	 * Comment for method inside level 2 nested class
        	 */
        	public int getInnerLevel2Field() {
        		return innerField;
        	}
    	}
    	
    }
}
