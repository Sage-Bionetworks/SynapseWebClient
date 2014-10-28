package org.sagebionetworks.web.client.widget.entity.menu.v2;

import static org.sagebionetworks.web.client.widget.entity.menu.v2.ActionType.*;

import org.gwtbootstrap3.client.ui.constants.IconType;

/**
 * The enumeration of possible actions.
 * 
 * @author John
 *
 */
public enum Action {

	TABLE_SCHEMA(IconType.LIST,"Show Schema", BUTTON),
	UPLOAD_TABLE_DATA(IconType.UPLOAD,"Upload Data", BUTTON),
	SHARE(IconType.LOCK,"Share", BUTTON),
	EDIT_TABLE_DATA(null,"Edit Table Data", TOOL_MENU),
	DOWNLOAD_TABLE_QUERY_RESULTS(IconType.DOWNLOAD,"Download Query Results", TOOL_MENU),
	CHANGE_ENTITY_NAME(null,"Change name", TOOL_MENU),
	SUBMIT_TO_CHALLENGE(null,"Submit to Challenge", TOOL_MENU),
	MOVE_ENTITY(null,"Move", TOOL_MENU),
	DELETE_ENTITY(IconType.TRASH_O,"Delete", TOOL_MENU);
	
	IconType icon;
	String text;
	ActionType type;
	
	Action(IconType icon, String text, ActionType type){
		this.icon = icon;
		this.text = text;
		this.type = type;
	}

	/**
	 * The icon that should be used for this action.
	 * @return
	 */
	public IconType getIcon() {
		return icon;
	}

	/**
	 * The text value for this action.
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * The type of action (butto or tool menu item).
	 * @return
	 */
	public ActionType getType() {
		return type;
	}
	
	
}
