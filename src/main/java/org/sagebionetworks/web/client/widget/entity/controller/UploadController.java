package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;

public interface UploadController {

	/**
	 * Check that the user can upload data to a container.
	 * 
	 * @param parentId The ID entity upload destination.
	 * @param entityClassName The full class name of the entity to be created.
	 * @param callback
	 */
	public void checkUploadToEntity(EntityBundle bundle, Callback callback);
}
