package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.Collection;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;

import com.google.inject.Inject;

/**
 * Licensed Downloader Presenter
 * @author dburdick
 *
 */
@Deprecated
public class LicensedDownloader implements LicensedDownloaderView.Presenter {
	
	private LicensedDownloaderView view;
	
	private GlobalApplicationState globalApplicationState;
	private AccessRequirement accessRequirementToDisplay;
	private String entityId;
	private EntityUpdatedHandler handler;
	
	private AuthenticationController authenticationController;
	private AccessRequirementDialog accessRequirementDialog;
	
	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			AccessRequirementDialog accessRequirementDialog) {
		this.view = view;		
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.accessRequirementDialog = accessRequirementDialog;
		view.setPresenter(this);		
	}

	// this method could be public but it's only used privately (when a ToU agreement
	// is created) so for now it's private
	private void fireEntityUpdatedEvent() {
		handler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	@Override
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.handler = handler;
	}

	/**
	 * Use with your own download button/link. 
	 * @param entity
	 * @param showDownloadLocations
	 */
	public void configure(EntityBundle entityBundle) {
		view.setPresenter(this);
		this.entityId = entityBundle.getEntity().getId();
		extractBundle(entityBundle);
	}
	
	private void extractBundle(EntityBundle entityBundle) {
		List<AccessRequirement> ars = entityBundle.getAccessRequirements();
		List<AccessRequirement> unmetARs = entityBundle.getUnmetAccessRequirements();
		// first, clear license agreement.  then, if there is an agreement required, set it below
		setLicenseAgreement(ars, unmetARs);
	}
	
	public boolean isDownloadAllowed() {
		if(authenticationController.isLoggedIn()) {
			return true;
		}
		view.showInfo("Login Required", "Please Login to download data.");
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
		return false;
	}

	public void onDownloadButtonClicked() {
		if (!isDownloadAllowed()) return;
		
		//show access restrictions dialog
		Callback finishedCallback = new Callback() {
			@Override
			public void invoke() {
				accessRequirementDialog.hide();
				fireEntityUpdatedEvent();
			}
		};
		
		accessRequirementDialog.configure(
				accessRequirementToDisplay, 
				entityId, 
				false, /*hasAdministrativeAccess*/
				false, /*accessApproved*/
				finishedCallback, /*entity updated callback*/
				finishedCallback /*on hide dialog callback*/);
		accessRequirementDialog.show();
	}
	
	public void setLicenseAgreement(Collection<AccessRequirement> allARs, Collection<AccessRequirement> unmetARs) {
		accessRequirementToDisplay = GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs);
	}
		
	public void clear() {
		view.clear();
	}
}
