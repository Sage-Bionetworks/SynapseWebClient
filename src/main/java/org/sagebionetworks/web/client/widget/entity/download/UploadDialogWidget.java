package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
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

	public void configure(String title, Entity entity, String parentEntityId, EntityUpdatedHandler handler, final CallbackP<String> fileHandleIdCallback, boolean isEntity){
		Widget body = uploader.asWidget(entity, parentEntityId, fileHandleIdCallback,isEntity);
		view.configureDialog(title, body);
		uploader.clearHandlers();
		// add user defined handler
		if (handler != null)
			uploader.addPersistSuccessHandler(handler);
		
		// add handlers for closing the window
		uploader.addPersistSuccessHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				view.hideDialog();
			}
		});
		uploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				view.hideDialog();
			}
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
