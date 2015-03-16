package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerViewImpl implements
		EntityActionControllerView {

	public interface Binder extends
			UiBinder<Widget, EntityActionControllerViewImpl> {
	}

	@UiField
	SimplePanel aclPanel;
	
	Widget widget;
	
	@Inject
	public EntityActionControllerViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showConfirmDialog(String title, String string, Callback callback) {
		DisplayUtils.showConfirmDialog(title, string, callback);
	}

	@Override
	public void showInfo(String tile, String message) {
		DisplayUtils.showInfo(tile, message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addAccessControlListModalWidget(
			IsWidget accessControlListModalWidget) {
		aclPanel.add(accessControlListModalWidget);
	}

}
