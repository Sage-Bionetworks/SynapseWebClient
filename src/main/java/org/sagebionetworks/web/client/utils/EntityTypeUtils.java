package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.schema.ObjectSchema;
/**
 * Extracted from DispalyUtils.
 *
 */
public class EntityTypeUtils {
	
	public static String getEntityTypeDisplay(ObjectSchema schema) {
		String title = schema.getTitle();
		if(title == null){
			title = "<Title missing for Entity: "+schema.getId()+">";
		}
		return title;
	}
}
