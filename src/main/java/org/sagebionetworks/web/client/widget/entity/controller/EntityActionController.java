package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;

public interface EntityActionController {

	/**
	 * Configure this controller for a given entity
	 * @param actionMenu
	 * @param entityBundle
	 * @param entityType
	 */
	public void configure(ActionMenuWidget actionMenu, EntityBundle entityBundle, boolean isUserAuthenticated);
	
	/**
	 * Delete action selected
	 */
	public void onDeleteEntity();
	
	/**
	 * The user has confrimed they want to delete the entity.
	 */
	public void onConfirmedDeleteEntity();
	
	/**
	 * Called if the user passes the pre-flight check to delete an entity.s
	 */
	public void onPreflightCheckedDeleteEntity();
}
