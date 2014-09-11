package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadDialogWidgetViewImpl extends Composite implements UploadDialogWidgetView {
	private Presenter presenter;
	
	@UiField
	SimplePanel dialogContainer;
	
	public interface Binder extends UiBinder<Widget, UploadDialogWidgetViewImpl> {}
	
	@Inject
	public UploadDialogWidgetViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setUploadDialog(Dialog uploadDialog) {
		dialogContainer.setWidget(uploadDialog);
	}
	
}
