package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

public class EntityActionControllerViewImpl implements EntityActionControllerView {
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showConfirmDialog(String title, String string,Callback callback) {
		DisplayUtils.showConfirmDialog(title, string, callback);
		
	}
	
	@Override
	public void showInfo(String tile, String message) {
		DisplayUtils.showInfo(tile, message);
	}

}
