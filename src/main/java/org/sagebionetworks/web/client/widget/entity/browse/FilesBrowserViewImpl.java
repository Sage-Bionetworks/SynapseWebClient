package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl implements FilesBrowserView {

	public interface FilesBrowserViewImplUiBinder extends
			UiBinder<Widget, FilesBrowserViewImpl> {
	}

	private EntityTreeBrowser entityTreeBrowser;
	private Widget widget;

	@UiField
	SimplePanel files;

	@Inject
	public FilesBrowserViewImpl(FilesBrowserViewImplUiBinder binder,
			EntityTreeBrowser entityTreeBrowser) {
		widget = binder.createAndBindUi(this);
		this.entityTreeBrowser = entityTreeBrowser;
		Widget etbW = entityTreeBrowser.asWidget();
		etbW.addStyleName("margin-top-10");
		files.setWidget(etbW);
	}

	@Override
	public void configure(String entityId) {
		entityTreeBrowser.configure(entityId);
	}
	
	public void setEntitySelectedHandler(org.sagebionetworks.web.client.events.EntitySelectedHandler handler) {
		entityTreeBrowser.setEntitySelectedHandler(handler);
	};
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {

	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		entityTreeBrowser.clear();
	}

	@Override
	public void refreshTreeView(String entityId) {
		entityTreeBrowser.configure(entityId);
	}
}
