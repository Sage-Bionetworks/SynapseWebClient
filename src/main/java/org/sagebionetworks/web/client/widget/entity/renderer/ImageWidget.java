package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Iterator;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.EntityWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidget implements ImageWidgetView.Presenter, WidgetRendererPresenter {
	
	private ImageWidgetView view;
	private Map<String,String> descriptor;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	
	@Inject
	public ImageWidget(ImageWidgetView view, SynapseClientAsync synapseClient, NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final String ownerObjectId, String ownerObjectType, Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		//TODO: change to support other object types
		synapseClient.getEntity(ownerObjectId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					if (entity.getAttachments() != null && entity.getAttachments().size() > 0) {
						for (Iterator iterator = entity.getAttachments().iterator(); iterator
								.hasNext();) {
							AttachmentData data = (AttachmentData) iterator.next();
							if (descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY).equals(data.getName()))
								view.configure(ownerObjectId, data, descriptor.get(WidgetConstants.IMAGE_WIDGET_WIDTH_KEY));				
						}
					}
				} catch (JSONObjectAdapterException e) {
//					view.showError(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION); 
				}
			}
			@Override
			public void onFailure(Throwable caught) {
//				view.showError(caught.getMessage());
			}			
		});
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
