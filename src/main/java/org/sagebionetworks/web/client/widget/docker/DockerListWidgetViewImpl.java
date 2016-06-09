package org.sagebionetworks.web.client.widget.docker;

import org.gwtbootstrap3.client.ui.Column;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerListWidgetViewImpl implements DockerListWidgetView {
	@UiField
	Column dockerList;

	Widget widget;
	Presenter presenter;
	
	public interface Binder extends UiBinder<HTMLPanel, DockerListWidgetViewImpl> {}

	@Inject
	public DockerListWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		dockerList.clear();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

}
