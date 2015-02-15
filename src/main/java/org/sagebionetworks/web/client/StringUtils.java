package org.sagebionetworks.web.client;
/**
 * Utilities for working with strings.
 * 
 * @author jhill
 *
 */
public class StringUtils {

	/**
	 * Compare two cell values and decide if there is a change.
	 * This method will treat empty string and null as equal.
	 * Also non-null strings are trimmed before compared.
	 * @param original
	 * @param changed
	 * @return
	 */
	public static boolean isValueChanged(String inOriginal, String inChanged){
		String tOriginal = trimWithEmptyAsNull(inOriginal);
		String tChanged = trimWithEmptyAsNull(inChanged);
		if(tOriginal == null){
			if(tChanged == null){
				return false;
			}
			return true;
		}else{
			return !tOriginal.equals(tChanged);
		}
	}
	/**
	 * @return Returns null if the passed value is null.  Returns null if the trimmed string is empty else the trimmed string.  
	 */
	public static String trimWithEmptyAsNull(String toUpdate){
		if(toUpdate == null){
			return null;
		}else{
			String trim = toUpdate.trim();
			if(trim.isEmpty()){
				return null;
			}else{
				return trim;
			}
		}
	}
	
}
