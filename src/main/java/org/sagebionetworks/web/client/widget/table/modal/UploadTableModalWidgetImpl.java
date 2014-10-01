package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget, UploadTableModalView.Presenter {
	
	String parentId;
	TableCreatedHandler handler;
	String fileHandleId;
	UploadTableModalView view;
	Uploader uploader;

	@Inject
	public UploadTableModalWidgetImpl(UploadTableModalView view, Uploader uploader) {
		this.view = view;
		this.uploader = uploader;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure(String parentId, TableCreatedHandler handler) {
		this.parentId = parentId;
		this.handler = handler;
	}

	@Override
	public void showModal() {
		Widget uploadWidget = uploader.asWidget(null, parentId, new CallbackP<String>() {
			@Override
			public void invoke(String fileHandlId) {
				onFileHandleCreated(fileHandlId);
			}
		}, false);
		view.setBody(uploadWidget);
		view.showModal();
	}
	
	/**
	 * Called when the file handle is created.
	 * @param fileHandlId
	 */
	private void onFileHandleCreated(String fileHandlId){
		
	}


}
