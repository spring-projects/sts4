package org.springframework.ide.si.view;

import org.eclipse.sprotty.SNode;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class IconElement extends SNode {
	
	  private String code;
	  
	  public IconElement() {
	  }
	  	  
	  public String getCode() {
	    return this.code;
	  }
	  
	  public void setCode(final String code) {
	    this.code = code;
	  }
	  
	  @Override
	  public String toString() {
	    return new ToStringBuilder(this)
	    	.addAllFields()
	    	.skipNulls()
	    	.toString();
	  }
}
