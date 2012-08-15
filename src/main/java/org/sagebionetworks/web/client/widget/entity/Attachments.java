package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Attachments implements AttachmentsView.Presenter,
		SynapseWidgetPresenter {

	private AttachmentsView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private EntityTypeProvider entityTypeProvider;
	private JSONObjectAdapter jsonObjectAdapter;
	private EventBus bus;

	private Entity entity;

	@Inject
	public Attachments(AttachmentsView view, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator,
			EntityTypeProvider entityTypeProvider,
			JSONObjectAdapter jsonObjectAdapter,
			EventBus bus) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.entityTypeProvider = entityTypeProvider;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.bus = bus;

		view.setPresenter(this);
	}

	public void configure(String baseUrl, Entity entity) {
		this.entity = entity;
		view.configure(baseUrl, entity.getId(), entity.getAttachments());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void deleteAttachment(final String tokenId) {
		List<AttachmentData> attachments = entity.getAttachments();
		if(tokenId != null) {
			// find attachment via token and remove it
			AttachmentData found = null; 
			for(AttachmentData data : attachments) {
				if(tokenId.equals(data.getTokenId())) {
					found = data;
				}
			}
			
			if(found != null) {
				// save name and remove from entity
				final String deletedName = found.getName();
				attachments.remove(found);
				JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
				try {
					entity.writeToJSONObject(adapter);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
					return;
				}

				// update entity minus attachment
				synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, false, new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {						
						view.attachmentDeleted(tokenId, deletedName);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
							view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
						}						
					}
				});
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
			}
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
		}
	}

}
