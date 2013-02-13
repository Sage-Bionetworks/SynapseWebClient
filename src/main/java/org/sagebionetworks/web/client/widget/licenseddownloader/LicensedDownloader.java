package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.Collection;
import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.LicenseAgreement;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Licensed Downloader Presenter
 * @author dburdick
 *
 */
public class LicensedDownloader implements LicensedDownloaderView.Presenter, SynapseWidgetPresenter {
	
	private LicensedDownloaderView view;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;

	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	
	private AccessRequirement accessRequirement;
	private String entityId;
	private UserProfile userProfile;
	
	private HandlerManager handlerManager;
	
	private StackConfigServiceAsync stackConfigService;
	private JiraURLHelper jiraUrlHelper;
	
	// for testing
	public void setUserProfile(UserProfile userProfile) {this.userProfile=userProfile;}
	public void setAccessRequirement(AccessRequirement accessRequirement) {this.accessRequirement=accessRequirement;}

	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseClientAsync synapseClient,
			JiraURLHelper jiraUrlHelper,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.jiraUrlHelper=jiraUrlHelper;
		view.setPresenter(this);		
		clearHandlers();
	}

	// this method could be public but it's only used privately (when a ToU agreement
	// is created) so for now it's private
	private void fireEntityUpdatedEvent() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	/*
	 * Public methods
	 */
	@Override
	public void clearHandlers() {
		handlerManager = new HandlerManager(this);
	}

	@Override
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {		
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	/**
	 * Use with your own download button/link. 
	 * @param entity
	 * @param showDownloadLocations
	 */
	public void configureHeadless(EntityBundle entityBundle, UserProfile userProfile) {
		view.setPresenter(this);
		this.entityId = entityBundle.getEntity().getId();
		extractBundle(entityBundle, userProfile);
	}
	
	private void extractBundle(EntityBundle entityBundle, UserProfile userProfile) {
		Entity entity = entityBundle.getEntity();
		loadDownloadLocations(entity);		
		List<AccessRequirement> ars = entityBundle.getAccessRequirements();
		List<AccessRequirement> unmetARs = entityBundle.getUnmetAccessRequirements();
		this.userProfile = userProfile;
		// first, clear license agreement.  then, if there is an agreement required, set it below
		setLicenseAgreement(ars, unmetARs);
	}
	
	/**
	 * Returns a standard download button
	 * @param entity
	 * @param showDownloadLocations
	 * @return
	 */
	public Widget asWidget(EntityBundle entityBundle, UserProfile userProfile) {
		configureHeadless(entityBundle, userProfile);
		
		return view.asWidget();
	}	
	
	/**
	 * does nothing use asWidget(Entity entity) 
	 */
	@Override
	public Widget asWidget() { 
		return null;
	}	

		
	
	/**
	 * Loads the download locations for the given Layer 
	 * @param entity Layer model object
	 */
	public void loadDownloadLocations(final Entity entity) {		
		if(entity != null) {
			view.showDownloadsLoading();
			if(entity instanceof Locationable) {
				Locationable locationable = (Locationable)entity;
				List<LocationData> locations = locationable.getLocations();				
				if (this.authenticationController.isLoggedIn()) {
					if(locations != null && locations.size() > 0) {
						this.view.setDownloadLocations(locations, locationable.getMd5());
					} else {
						this.view.setNoDownloads();
					}
				} else {
					this.view.setNeedToLogIn();
				}
			} else {
				view.setNoDownloads();
			}
		}
	}		

	public boolean isDownloadAllowed() {
		if(authenticationController.isLoggedIn()) {
			return true;
		}
		view.showInfo("Login Required", "Please Login to download data.");
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN));
		return false;
	}
		
	public void showWindow() {
		this.view.showWindow();
	}
	
	public void setLicenseAgreement(Collection<AccessRequirement> allARs, Collection<AccessRequirement> unmetARs) {
		AccessRequirement arToDisplay = GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs);
		setRestrictionLevel(GovernanceServiceHelper.entityRestrictionLevel(allARs));
		setApprovalType(GovernanceServiceHelper.accessRequirementApprovalType(arToDisplay));
		
		if (arToDisplay!=null) {
			String licenseAgreementText = null;
			if (arToDisplay instanceof TermsOfUseAccessRequirement) {
				// for tier 2 requirements, set license agreement
				licenseAgreementText = ((TermsOfUseAccessRequirement)arToDisplay).getTermsOfUse();
			} else if (arToDisplay instanceof ACTAccessRequirement) {
				// for tier 3 requirements, set ACT contact instructions
				licenseAgreementText = ((ACTAccessRequirement)arToDisplay).getActContactInfo();
			} else {
				view.showInfo("Error", arToDisplay.getClass().toString());
			}
			LicenseAgreement licenseAgreement = new LicenseAgreement();
			licenseAgreement.setLicenseHtml(licenseAgreementText);
			view.setLicenseHtml(licenseAgreement.getLicenseHtml());
		}
	}
	
	public void showLoading() {
		this.view.showDownloadsLoading();
	}
		
	public void clear() {
		view.clear();
	}
	
	public void setRestrictionLevel(RESTRICTION_LEVEL restrictionLevel) {
		this.view.setRestrictionLevel(restrictionLevel);
	}
	
	public void setApprovalType(APPROVAL_TYPE approvalType) {
		this.view.setApprovalType(approvalType);
	}
	
	@Override
	public Callback getTermsOfUseCallback() {
		return new Callback() {public void invoke() {setLicenseAccepted();}};
	}
	
	@Override
	public void setLicenseAccepted() {	
		Callback onSuccess = new Callback() {
			@Override
			public void invoke() {
				fireEntityUpdatedEvent();
			}
		};
		CallbackP<Throwable> onFailure = new CallbackP<Throwable>() {
			@Override
			public void invoke(Throwable t) {
				view.showInfo("Error", t.getMessage());
			}
		};
		GovernanceServiceHelper.signTermsOfUse(
				userProfile.getOwnerId(), 
				accessRequirement.getId(), 
				onSuccess, 
				onFailure, 
				synapseClient, 
				jsonObjectAdapter);
	}
	
	@Override
	public Callback getRequestAccessCallback() {
		final String jiraLink = jiraUrlHelper.createRequestAccessIssue(
				userProfile.getOwnerId(), 
				userProfile.getDisplayName(), 
				userProfile.getUserName(), 
				entityId, 
				accessRequirement.getId().toString());
		
		return new Callback() {
			@Override
			public void invoke() {
				Window.open(jiraLink, "_blank", "");
			}};
	}

}
