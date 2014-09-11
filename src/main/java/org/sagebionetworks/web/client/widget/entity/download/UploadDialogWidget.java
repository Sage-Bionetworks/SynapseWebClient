package org.sagebionetworks.web.client.widget.entity.download;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadDialogWidget implements UploadDialogWidgetView.Presenter, SynapseWidgetPresenter {
	private UploadDialogWidgetView view;
	private Uploader uploader;
	private Dialog uploadDialog;
	@Inject
	public UploadDialogWidget(UploadDialogWidgetView view, Uploader uploader, Dialog uploadDialog) {
		this.view = view;
		this.uploader = uploader;
		this.uploadDialog = uploadDialog;
		view.setPresenter(this);
		view.setUploadDialog(uploadDialog);
		uploadDialog.setSize(ModalSize.LARGE);
	}
		
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(String title, Entity entity, String parentEntityId, EntityUpdatedHandler handler, final CallbackP<String> fileHandleIdCallback){
		boolean isEntity = entity != null;
		Widget body = uploader.asWidget(entity, parentEntityId, fileHandleIdCallback,isEntity);
		uploadDialog.configure(title, body, null, null, null, false);
		uploader.clearHandlers();
		// add user defined handler
		if (handler != null)
			uploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				uploadDialog.hide();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				uploadDialog.hide();
			}
		});
	}
	
	public void disableMultipleFileUploads() {
		uploader.disableMultipleFileUploads();
	}
	
	public void show() {
		uploadDialog.show();
	}
}
