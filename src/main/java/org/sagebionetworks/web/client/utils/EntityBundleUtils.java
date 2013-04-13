package org.sagebionetworks.web.client.utils;
import org.sagebionetworks.web.client.model.EntityBundle;
/**
 * Extracted from DispalyUtils
 * 
 *
 */
public class EntityBundleUtils {
	
	/**
	 * Does this entity have attachmet previews?
	 * @param entity
	 * @return
	 */
	public static boolean hasChildrenOrPreview(EntityBundle bundle){
		if(bundle == null) return true;
		if(bundle.getEntity() == null) return true;
		Boolean hasChildern = bundle.getHasChildren();
		if(hasChildern == null) return true;
		return hasChildern;
	}
}
