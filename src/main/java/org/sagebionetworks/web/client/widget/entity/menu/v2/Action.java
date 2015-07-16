package org.sagebionetworks.web.client.widget.entity.menu.v2;


/**
 * The enumeration of possible actions of an entity action menu.
 * 
 * @author John
 *
 */
public enum Action {

	// Table specific
	TOGGLE_TABLE_SCHEMA,
	UPLOAD_TABLE_DATA,
	EDIT_TABLE_DATA,
	DOWNLOAD_TABLE_QUERY_RESULTS,
	// All entity
	SHARE,
	CHANGE_ENTITY_NAME,
	CHANGE_STORAGE_LOCATION,
	SUBMIT_TO_CHALLENGE,
	MOVE_ENTITY,
	DELETE_ENTITY,
	EDIT_WIKI_PAGE,
	VIEW_WIKI_SOURCE,
	ADD_WIKI_SUBPAGE,
	CREATE_LINK,
	TOGGLE_ANNOTATIONS,
	UPLOAD_NEW_FILE,
	EDIT_PROVENANCE,
	CREATE_DOI
}
