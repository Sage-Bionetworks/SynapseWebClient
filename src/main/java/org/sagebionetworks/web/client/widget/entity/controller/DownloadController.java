package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.repo.model.EntityBundle;

/**
 * Can the user download from this entity.
 * 
 * @author jhill
 *
 */
public interface DownloadController {

	/**
	 * Can the user download from the entity.
	 * @param uploadTo
	 * @param callback
	 */
	public void checkDownloadFromEntity(EntityBundle uploadTo, Callback callback);
	
	/**
	 * Does the user have an unmet download restriction on this entity?
	 * @param uploadTo
	 * @return
	 */
	public boolean hasUnmetDownloadRestriction(EntityBundle uploadTo);
}
