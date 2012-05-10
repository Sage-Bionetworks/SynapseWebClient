package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import java.util.List;

/**
 * Utilities for generating the display string for various object types.
 * 
 * @author jmhill
 *
 */
public class StringValueUtils {
	
	public static final int MAX_CHARS_IN_LIST = 50;

	/**
	 * Convert the value to a short string to be shown in the table.
	 * @param value
	 * @return
	 */
	public static String valueToString(Object value){
		if(value == null) return null;
		if(value instanceof String){
			String stringValue =  (String) value;
			if(stringValue.length() > MAX_CHARS_IN_LIST){
				return stringValue.substring(0,MAX_CHARS_IN_LIST-1);
			}else{
				return stringValue;
			}
		}else if(value instanceof Long){
			return value.toString();
		}else if(value instanceof Double){
			return value.toString();
		}else if(value instanceof Date){
			return value.toString();
		}else if(value instanceof List<?>){
			StringBuilder builder = new StringBuilder();
			List list = (List) value;
			int index =0;
			for(Object child: list){
				if(index > 0){
					builder.append(", ");
				}
				builder.append(valueToString(child));
				if(builder.length() > MAX_CHARS_IN_LIST){
					return builder.toString();
				}
				index++;
			}
			return builder.toString();
		}else{
			throw new IllegalArgumentException("Unknown type: "+value.toString());
		}
	}
	
	/**
	 * Convert the value to a short string to be shown in the table.
	 * @param value
	 * @return
	 */
	public static String valueToToolTips(Object value){
		if(value == null) return null;
		if(value instanceof String){
			return (String) value;
		}else if(value instanceof Long){
			return value.toString();
		}else if(value instanceof Double){
			return value.toString();
		}else if(value instanceof Date){
			return value.toString();
		}else if(value instanceof List<?>){
			StringBuilder builder = new StringBuilder();
			builder.append("<ul>");
			List list = (List) value;
			for(Object child: list){
				builder.append("<li>");
				builder.append(valueToToolTips(child));
				builder.append("</li>");
			}
			builder.append("</ul>");
			return builder.toString();
		}else{
			throw new IllegalArgumentException("Unknown type: "+value.toString());
		}
	}
}
