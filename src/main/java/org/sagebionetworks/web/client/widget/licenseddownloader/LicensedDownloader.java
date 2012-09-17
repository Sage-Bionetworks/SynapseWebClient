package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.FileDownload;
import org.sagebionetworks.web.shared.LicenseAgreement;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
	
	private HandlerManager handlerManager = new HandlerManager(this);
	
	private StackConfigServiceAsync stackConfigService;
	private JiraURLHelper jiraUrlHelper;

	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseClientAsync synapseClient,
			StackConfigServiceAsync stackConfigService,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.stackConfigService = stackConfigService;
		stackConfigService.getJiraGovernanceProjectId(new AsyncCallback<Integer>(){
			@Override
			public void onFailure(Throwable caught) {
				// no op
			}
			@Override
			public void onSuccess(Integer result) {
				jiraUrlHelper = new JiraURLHelper(result);
			}});
		view.setPresenter(this);		
	}

	// this method could be public but it's only used privately (when a ToU agreement
	// is created) so for now it's private
	private void fireEntityUpdatedEvent() {
		handlerManager.fireEvent(new EntityUpdatedEvent());
	}
	
	/*
	 * Public methods
	 */
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {		
		handlerManager.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	/**
	 * Use with your own download button/link. 
	 * @param entity
	 * @param showDownloadLocations
	 */
	public void configureHeadless(Entity entity) {
		view.setPresenter(this);
		this.entityId = entity.getId();
		refresh(null);
	}
	
	public void unmetAccessRequirementsCallback(AccessRequirementsTransport result, Callback callback) {
		Entity entity = null;
		VariableContentPaginatedResults<AccessRequirement> ars = new VariableContentPaginatedResults<AccessRequirement>();
		try {
			entity = nodeModelCreator.createEntity(result.getEntityString(), result.getEntityClassAsString());
			loadDownloadLocations(entity);		
			ars = nodeModelCreator.initializeEntity(result.getAccessRequirementsString(), ars);
			
			userProfile = nodeModelCreator.createEntity(result.getUserProfileString(), UserProfile.class);
		} catch (RestServiceException e) {
			throw new RuntimeException(e);
		}
		// first, clear license agreement.  then, if there is an agreement required, set it below
		setLicenseAgreement(null, null);
		for (AccessRequirement ar : ars.getResults()) {
			if (ar instanceof TermsOfUseAccessRequirement) {
				// for tier 2 requirements, set license agreement
				String touContent = ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
				LicenseAgreement licenseAgreement = new LicenseAgreement();
				// TODO support option in which TOU is in a location
				licenseAgreement.setLicenseHtml(touContent);
				setLicenseAgreement(licenseAgreement, ar);
				break;
			} else if (ar instanceof ACTAccessRequirement) {
				// for tier 3 requirements, set ACT contact instructions
				String actContactInfo = ((ACTAccessRequirement)ar).getActContactInfo();
				LicenseAgreement licenseAgreement = new LicenseAgreement();
				// TODO support option in which ACT contact info is in a location
				licenseAgreement.setLicenseHtml(actContactInfo);
				setLicenseAgreement(licenseAgreement, ar);
				break;
		} else {
			view.showInfo("Error", ar.getClass().toString());
			}
		}
		if (callback!=null) callback.invoke();		
	}
	
	/**
	 * Check for unmet access requirements and refresh accordingly
	 *
	 * @param callback an optional step to call after successful completion, or null if none
	 */
	public void refresh(final Callback callback) {
		synapseClient.getUnmetAccessRequirements(entityId, new AsyncCallback<AccessRequirementsTransport>(){

			@Override
			public void onSuccess(AccessRequirementsTransport result) {
				unmetAccessRequirementsCallback(result, callback);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showInfo("Error", caught.getMessage());
			}

			
		});
	}
	
	/**
	 * Returns a standard download button
	 * @param entity
	 * @param showDownloadLocations
	 * @return
	 */
	public Widget asWidget(Entity entity) {
		configureHeadless(entity);
		
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
	
	public void setLicenseAgreement(LicenseAgreement agreement, AccessRequirement ar) {
		accessRequirement = ar;
		if (agreement != null && accessRequirement!=null) {
			view.setLicenseHtml(agreement.getLicenseHtml());
			if (accessRequirement instanceof TermsOfUseAccessRequirement) {
				this.setRequireApproval(APPROVAL_REQUIRED.LICENSE_ACCEPTANCE);
			} else if (accessRequirement instanceof ACTAccessRequirement) {
				this.setRequireApproval(APPROVAL_REQUIRED.ACT_APPROVAL);
			} else {
				throw new IllegalArgumentException(ar.getClass().toString());
			}
		} else {
			this.setRequireApproval(APPROVAL_REQUIRED.NONE);
		}
	}
	
	@Deprecated
	public void setDownloadUrls(List<FileDownload> downloads) {		
		this.view.setDownloadUrls(downloads);
	}
	
	public void showLoading() {
		this.view.showDownloadsLoading();
	}
		
	public void clear() {
		view.clear();
	}
	
	public void setRequireApproval(APPROVAL_REQUIRED licenseApproval) {
		this.view.setApprovalRequired(licenseApproval);
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
//				refresh(new Callback() {
//					public void invoke() {showWindow();}
//					
//				});
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
