package org.sagebionetworks.web.client.widget.entity.dialog;

/**
 * The annotation types.
 * @author John
 *
 */
public enum ANNOTATION_TYPE {
	STRING("Text"),
	LONG("Integer"),
	DOUBLE("Floating Point"),
	DATE("Date");
	
	private String displayText;
	ANNOTATION_TYPE(String displayText){
		this.displayText = displayText;
	}
	
	/**
	 * This display text for this option.
	 * @return
	 */
	public String getDisplayText(){
		return displayText;
	}
	
	public static ANNOTATION_TYPE getTypeForDisplay(String display){
		for(ANNOTATION_TYPE type: values()){
			if(type.displayText.equals(display)) return type;
		}
		throw new IllegalArgumentException("Cannot find type for display: "+display);
	}
}