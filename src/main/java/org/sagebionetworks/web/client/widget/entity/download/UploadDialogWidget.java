package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadDialogWidget implements UploadDialogWidgetView.Presenter, SynapseWidgetPresenter {
	private UploadDialogWidgetView view;
	private Uploader uploader;
	@Inject
	public UploadDialogWidget(UploadDialogWidgetView view, Uploader uploader) {
		this.view = view;
		this.uploader = uploader;
		view.setPresenter(this);
	}
		
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(String title, Entity entity, String parentEntityId, final CallbackP<String> fileHandleIdCallback, boolean isEntity){
		Widget body = uploader.configure(entity, parentEntityId, fileHandleIdCallback,isEntity);
		view.configureDialog(title, body);
		
		// add handlers for closing the window
		uploader.setSuccessHandler(() -> {
			view.hideDialog();
		});
		
		uploader.setCancelHandler(() -> {
			view.hideDialog();
		});
	}
	
	public void disableMultipleFileUploads() {
		uploader.disableMultipleFileUploads();
	}
	
	public void setUploaderLinkNameVisible(boolean visible) {
		uploader.setUploaderLinkNameVisible(visible);
	}
	
	public void show() {
		view.showDialog();
	}
}
