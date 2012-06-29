package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.TermsOfUseAccessApproval;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.VariableContentPaginatedResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.AccessRequirementsTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.FileDownload;
import org.sagebionetworks.web.shared.LicenseAgreement;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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
	
	private Long accessRequirementId = null;
	private String entityId;
	private String userPrincipalId;
	
	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);		
	}

	/*
	 * Public methods
	 */		

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
	
	interface Callback {
		void invoke();
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
				Entity entity = null;
				VariableContentPaginatedResults<AccessRequirement> ars = new VariableContentPaginatedResults<AccessRequirement>();
				try {
					entity = nodeModelCreator.createEntity(result.getEntityString(), result.getEntityClassAsString());
					loadDownloadLocations(entity);		
					ars = nodeModelCreator.initializeEntity(result.getAccessRequirementsString(), ars);
					
					UserProfile userProfile = nodeModelCreator.createEntity(result.getUserProfileString(), UserProfile.class);
					userPrincipalId = userProfile.getOwnerId();
				} catch (RestServiceException e) {
					throw new RuntimeException(e);
				}
				// first, clear license agreement.  then, if there is an agreement required, set it below
				setLicenseAgreement(null);
				for (AccessRequirement ar : ars.getResults()) {
					if (ar instanceof TermsOfUseAccessRequirement) {
						// for tier 2 requirements, set license agreement
						// TODO handle multiple requirements
						String touContent = ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
						LicenseAgreement licenseAgreement = new LicenseAgreement();
						// TODO support option in which TOU is in a location
						licenseAgreement.setLicenseHtml(touContent);
						licenseAgreement.setEulaId(ar.getId().toString());
						setLicenseAgreement(licenseAgreement);
						break;
					} else {
						onFailure(new IllegalArgumentException(ar.getClass().toString()));
					}
				}
				if (callback!=null) callback.invoke();
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
	
	public void hideWindow() {
		this.view.hideWindow();
	}
	
	public void setLicenseAgreement(LicenseAgreement agreement) {
		if (agreement != null) {
			if (agreement.getCitationHtml() != null) {
				view.setCitationHtml(agreement.getCitationHtml());
			}
			view.setLicenseHtml(agreement.getLicenseHtml());
			this.setRequireLicenseAcceptance(true);
			this.accessRequirementId = Long.parseLong(agreement.getEulaId());
		} else {
			this.setRequireLicenseAcceptance(false);
			this.accessRequirementId = null;
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
	
	public void setRequireLicenseAcceptance(boolean requireLicenseAcceptance) {
		this.view.setLicenceAcceptanceRequired(requireLicenseAcceptance);
	}
	
	@Override
	public void setLicenseAccepted() {		
		TermsOfUseAccessApproval agreement = new TermsOfUseAccessApproval();
		agreement.setAccessorId(userPrincipalId);
		agreement.setRequirementId(accessRequirementId);
		JSONObjectAdapter approvalJson = null;
		try {
			approvalJson = agreement.writeToJSONObject(jsonObjectAdapter.createNew());
		} catch (JSONObjectAdapterException e) {
			view.showInfo("Error", e.getMessage());
			return;
		}
		EntityWrapper ew = new EntityWrapper(approvalJson.toJSONString(), agreement.getClass().getName(), null);
		synapseClient.createAccessApproval(ew, new AsyncCallback<EntityWrapper>(){
			@Override
			public void onSuccess(EntityWrapper result) {
				refresh(new Callback() {
					public void invoke() {showWindow();}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showInfo("Error", caught.getMessage());
			}			
		});
	}

}
