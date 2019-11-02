package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;

public interface EntityCRUDController {

	/**
	 * Check that the user can create an entity.
	 * 
	 * @param parentEntity
	 * @param entityClassName The full class name of the entity to be created.
	 * @param callback
	 */
	public void checkCreateEntity(EntityBundle parentEntity, String entityClassName, Callback callback);

	/**
	 * Check that the user can delete the given entity.
	 * 
	 * @param toDelete
	 * @param callback
	 */
	public void checkDeleteEntity(EntityBundle toDelete, Callback callback);

	/**
	 * Check that the user can update an entity.
	 * 
	 * @param toDelete
	 * @param callback
	 */
	public void checkUpdateEntity(EntityBundle toUpdate, Callback callback);
}
