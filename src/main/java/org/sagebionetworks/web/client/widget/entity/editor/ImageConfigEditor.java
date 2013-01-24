package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ImageConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(String ownerObjectId, String ownerObjectType, Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		//TODO: change file upload to support other owner object types
		view.setEntityId(ownerObjectId);
		//if the attachmentData is set then there'a an associated image.  Only show the external url ui if we aren't editing one that already has an attachment
//		view.setExternalVisible(descriptor.getFileName() == null);
//		if (descriptor.getFileName() != null) {
//			//find the attachment associated with this file name
//			synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
//				@Override
//				public void onSuccess(EntityWrapper result) {
//					Entity entity;
//					try {
//						entity = nodeModelCreator.createEntity(result);
//						for (Iterator iterator = entity.getAttachments().iterator(); iterator
//								.hasNext();) {
//							AttachmentData data = (AttachmentData) iterator.next();
//							if (descriptor.getFileName().equals(data.getName()))
//								view.setUploadedAttachmentData(data);				
//						}
//					} catch (JSONObjectAdapterException e) {
//						view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION); 
//					}
//				}
//				@Override
//				public void onFailure(Throwable caught) {
//					view.showErrorMessage(caught.getMessage());
//				}			
//			});
//		}
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		if (!view.isExternal())
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, view.getUploadedAttachmentData().getName());
	}
	
	@Override
	public String getTextToInsert() {
		if (view.isExternal())
			return "!["+view.getAltText()+"]("+view.getImageUrl()+")";
		else return null;
	}

	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	/*
	 * Private Methods
	 */
}
