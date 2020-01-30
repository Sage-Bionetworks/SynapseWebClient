package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;


/**
 * Controller is an abstraction for all of preflight checks a user must pass before doing certain
 * operations. This includes access restrictions and user certification.
 * 
 * @author jhill
 *
 */
public interface PreflightController extends UploadController, DownloadController, EntityCRUDController {


	/**
	 * Check that the user can create an entity and upload data to that entity.
	 * 
	 * @param parentEntity
	 * @param entityClassName The full class name of the entity to be created.
	 * @param callback
	 */
	public void checkCreateEntityAndUpload(EntityBundle parentEntity, String entityClassName, Callback callback);
}
