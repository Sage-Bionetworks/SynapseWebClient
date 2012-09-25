package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.SynapsePersistable;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelperImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
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
		view.setPresenter(this);		
	}		
		
	public Widget asWidget(EntityBundle entityBundle, boolean showCancel) {
		this.entityBundle = entityBundle;
		this.view.createUploadForm(showCancel);
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
	public void setExternalLocation(String path) {
		// TODO : store 
		
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
				view.openNewTab(getJiraRestrictionLink());
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
