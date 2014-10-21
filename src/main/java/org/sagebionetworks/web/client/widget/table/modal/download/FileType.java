package org.sagebionetworks.web.client.widget.table.modal.download;


/**
 * File type for as CSV download.
 * 
 * @author jhill
 *
 */
public enum FileType {
	CSV("Comma Separated Values (CSV)", ","),
	TSV("Tab Separated Values (TSV)", "\t");
	
	private String dispalyValue;
	private String separator;
	
	FileType(String dispalyValue, String separator){
		this.dispalyValue = dispalyValue;
		this.separator = separator;
	}
	
	/**
	 * Value that can be shown to the user.
	 * @return
	 */
	public String getDispalyValue(){
		return this.dispalyValue;
	}
	
	/**
	 * The actual separtor
	 * @return
	 */
	public String getSeparator(){
		return this.separator;
	}
	/**
	 * Find a type for the display value
	 * @param dispalyValue
	 * @return
	 */
	public static FileType findType(String dispalyValue){
		for(FileType type: values()){
			if(type.dispalyValue.equals(dispalyValue)){
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown type: "+dispalyValue);
	}
}
