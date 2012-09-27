package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.EntityUtil;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableUploader implements LocationableUploaderView.Presenter, SynapseWidgetPresenter, SynapsePersistable {
	
	private LocationableUploaderView view;
	private NodeServiceAsync nodeService;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private HandlerManager handlerManager = new HandlerManager(this);
	private EntityBundle entityBundle;
	private EntityTypeProvider entityTypeProvider;
	private GlobalApplicationState globalApplicationState;
	private JSONObjectAdapter jsonObjectAdapter;

	private SynapseClientAsync synapseClient;
	private JiraURLHelper jiraURLHelper;
	
	@Inject
	public LocationableUploader(
			LocationableUploaderView view, 
			NodeServiceAsync nodeService, 
			NodeModelCreator nodeModelCreator, 
			AuthenticationController authenticationController, 
			EntityTypeProvider entityTypeProvider,
			SynapseClientAsync synapseClient,
			JiraURLHelper jiraURLHelper,
			JSONObjectAdapter jsonObjectAdapter
			) {
	
		this.view = view;
		this.nodeService = nodeService;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.entityTypeProvider = entityTypeProvider;
		this.synapseClient = synapseClient;
		this.jiraURLHelper = jiraURLHelper;
		this.jsonObjectAdapter=jsonObjectAdapter;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);		
	}		
		
	public Widget asWidget(EntityBundle entityBundle) {
		this.entityBundle = entityBundle;
		this.view.createUploadForm();
		return this.view.asWidget();
	}
		
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		handlerManager = new HandlerManager(this);		
		this.entityBundle = null;		
	}

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public String getUploadActionUrl(boolean isRestricted) {
		return GWT.getModuleBaseURL() + "upload" + "?" + 
			DisplayUtils.ENTITY_PARAM_KEY + "=" + entityBundle.getEntity().getId() + "&" +
			DisplayUtils.IS_RESTRICTED_PARAM_KEY + "=" +isRestricted;
	}

	@Override
	public void setExternalLocation(String path, final boolean isNewlyRestricted) {
		String entityId = entityBundle.getEntity().getId();
		
		synapseClient.updateExternalLocationable(entityId, path, new AsyncCallback<EntityWrapper>() {
			
			public void onSuccess(EntityWrapper result) {
				try {
					Entity entity = nodeModelCreator.createEntity(result, entityBundle.getEntity().getClass());
					if (isNewlyRestricted) {
						EntityWrapper arEW = null;
						try {
							arEW=EntityUtil.createLockDownDataAccessRequirementAsEntityWrapper(entity.getId(), jsonObjectAdapter);
						} catch (JSONObjectAdapterException caught) {
							view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);							
						}
						synapseClient.createAccessRequirement(arEW, new AsyncCallback<EntityWrapper>(){
							@Override
							public void onSuccess(EntityWrapper result) {
								view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
								entityUpdated();
							}
							@Override
							public void onFailure(Throwable caught) {
								view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
							}
						});
					} else {
						view.showInfo(DisplayConstants.TEXT_LINK_FILE, DisplayConstants.TEXT_LINK_SUCCESS);
						entityUpdated();						
					}
				} catch (RestServiceException e) {
					onFailure(null);					
				}
			};
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.TEXT_LINK_FAILED);
			}
		} ); 
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCancelHandler(CancelHandler handler) {
		handlerManager.addHandler(CancelEvent.getType(), handler);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addPersistSuccessHandler(EntityUpdatedHandler handler) {
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public void closeButtonSelected() {
		handlerManager.fireEvent(new CancelEvent());
	}

	public void entityUpdated() {
			handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	@Override
	public void handleSubmitResult(String resultHtml, boolean isNewlyRestricted) {
		if(resultHtml == null) resultHtml = "";
		// response from server 
		if(!resultHtml.contains(DisplayUtils.UPLOAD_SUCCESS)) {
			view.showErrorMessage(DisplayConstants.ERROR_UPLOAD);
			handlerManager.fireEvent(new CancelEvent());
		} else {
			view.showInfo(DisplayConstants.TEXT_UPLOAD_FILE, DisplayConstants.TEXT_UPLOAD_SUCCESS);
			if (isNewlyRestricted) {
				view.openNewBrowserTab(getJiraRestrictionLink());
			}
			handlerManager.fireEvent(new EntityUpdatedEvent());
		}
	}
	
	@Override
	public boolean isRestricted() {
		return entityBundle.getAccessRequirements().size() > 0;
	}
	
	
	@Override
	public String getJiraRestrictionLink() {
		UserProfile userProfile = authenticationController.getLoggedInUser().getProfile();
		if (userProfile==null) throw new NullPointerException("User profile cannot be null.");
		return jiraURLHelper.createAccessRestrictionIssue(
				userProfile.getUserName(), userProfile.getDisplayName(), entityBundle.getEntity().getId());
	}
}
