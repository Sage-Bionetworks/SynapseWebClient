package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.web.client.widget.modal.Dialog;

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
	Dialog uploadDialog;
	public interface Binder extends UiBinder<Widget, UploadDialogWidgetViewImpl> {}
	
	@Inject
	public UploadDialogWidgetViewImpl(Binder uiBinder, Dialog uploadDialog) {
		initWidget(uiBinder.createAndBindUi(this));
		this.uploadDialog = uploadDialog;
		uploadDialog.setSize(ModalSize.LARGE);
		dialogContainer.setWidget(uploadDialog);
		uploadDialog.setClosable(false);
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
	public void showInfo(String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configureDialog(String title, Widget body) {
		uploadDialog.configure(title, body, null, null, null, false);
	}
	
	@Override
	public void hideDialog() {
		uploadDialog.hide();
	}
	
	@Override
	public void showDialog() {
		uploadDialog.show();
	}
}
