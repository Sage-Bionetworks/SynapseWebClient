package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Iterator;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ImageConfigView view;
	private ImageAttachmentWidgetDescriptor descriptor;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.initView();
	}
	
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ImageAttachmentWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (ImageAttachmentWidgetDescriptor)widgetDescriptor;
		view.setEntityId(entityId);
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
			descriptor.setFileName(view.getUploadedAttachmentData().getName());
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
