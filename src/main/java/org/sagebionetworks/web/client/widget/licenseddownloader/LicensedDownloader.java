package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.Collection;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.GovernanceServiceHelper;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Licensed Downloader Presenter
 * @author dburdick
 *
 */
public class LicensedDownloader implements LicensedDownloaderView.Presenter, SynapseWidgetPresenter {
	
	private LicensedDownloaderView view;
	
	private GlobalApplicationState globalApplicationState;
	private GWTWrapper gwt;
	private AccessRequirement accessRequirementToDisplay;
	private String entityId;
	private APPROVAL_TYPE approvalType;
	private HandlerManager handlerManager;
	
	private boolean isDirectDownloadSupported;
	private AuthenticationController authenticationController;
	private SynapseJSNIUtils synapseJSNIUtils;
	private AccessRequirementDialog accessRequirementDialog;
	
	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseJSNIUtils synapseJSNIUtils,
			GWTWrapper gwt,
			AccessRequirementDialog accessRequirementDialog) {
		this.view = view;		
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.gwt = gwt;
		this.accessRequirementDialog = accessRequirementDialog;
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
	public void configure(EntityBundle entityBundle) {
		view.setPresenter(this);
		this.entityId = entityBundle.getEntity().getId();
		extractBundle(entityBundle);
	}
	
	private void extractBundle(EntityBundle entityBundle) {
		loadDownloadUrl(entityBundle);		
		List<AccessRequirement> ars = entityBundle.getAccessRequirements();
		List<AccessRequirement> unmetARs = entityBundle.getUnmetDownloadAccessRequirements();
		// first, clear license agreement.  then, if there is an agreement required, set it below
		setLicenseAgreement(ars, unmetARs);
	}
	
	/**
	 * If no access restrictions are present, then this will return the download url for the FileEntity FileHandle.  Otherwise, it will return null.
	 * @return
	 */
	public String getDirectDownloadURL() {
		if (isDirectDownloadSupported && authenticationController.isLoggedIn())
			return view.getDirectDownloadURL();
		else return null; 
	}
	
	/**
	 * Returns a standard download button.  User must configure this widget
	 * @param entity
	 * @param showDownloadLocations
	 * @return
	 */
	public Widget asWidget() {
		return view.asWidget();
	}	
			
	/**
	 * Loads the download url 
	 */
	public void loadDownloadUrl(final EntityBundle entityBundle) {		
		if(entityBundle != null && entityBundle.getEntity() != null) {
			view.showDownloadsLoading();
			if (entityBundle.getEntity() instanceof FileEntity) {
				FileEntity fileEntity = (FileEntity)entityBundle.getEntity();
				if (this.authenticationController.isLoggedIn()) {
					FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
					if (fileHandle != null) {
						String md5 = null;
						if (fileHandle instanceof S3FileHandleInterface) {
							md5 = ((S3FileHandleInterface)fileHandle).getContentMd5();
						}
						String directDownloadURL = getDirectDownloadURL(fileEntity, fileHandle);
						this.view.setDownloadLocation(md5, directDownloadURL);
					}
					else {
						this.view.setNoDownloads();
					}
				} else {
					this.view.setNeedToLogIn();
				}
			}
			//TODO: next block to be deleted
			else if(entityBundle.getEntity() instanceof Locationable) {
				Locationable locationable = (Locationable)entityBundle.getEntity();
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
		globalApplicationState.getPlaceChanger().goTo(new LoginPlace(ClientProperties.DEFAULT_PLACE_TOKEN));
		return false;
	}

	public void showWindow() {
		if (!isDownloadAllowed()) return;
		
		if (approvalType != APPROVAL_TYPE.NONE){
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
					null,
					finishedCallback /*on hide dialog callback*/);
			accessRequirementDialog.show();
		} else {
			this.view.showWindow();	
		}
	}
	
	public void setLicenseAgreement(Collection<AccessRequirement> allARs, Collection<AccessRequirement> unmetARs) {
		accessRequirementToDisplay = GovernanceServiceHelper.selectAccessRequirement(allARs, unmetARs);
		if (unmetARs==null || unmetARs.isEmpty()) {
			isDirectDownloadSupported = true;
			approvalType = APPROVAL_TYPE.NONE; 
		} else {
			isDirectDownloadSupported = false;
			approvalType = GovernanceServiceHelper.accessRequirementApprovalType(accessRequirementToDisplay);
		}
	}
	
	public void showLoading() {
		this.view.showDownloadsLoading();
	}
		
	public void clear() {
		view.clear();
	}
	
	public String getDirectDownloadURL(FileEntity fileEntity, FileHandle fileHandle) {
		String externalUrl = null;
		if (fileHandle instanceof ExternalFileHandle) {
			externalUrl = ((ExternalFileHandle) fileHandle).getExternalURL();
		}
		
		String directDownloadURL = null;
		if (externalUrl == null)
			directDownloadURL = DisplayUtils.createFileEntityUrl(synapseJSNIUtils.getBaseFileHandleUrl(), fileEntity.getId(), fileEntity.getVersionNumber(), false);
		else {
			if (externalUrl.toLowerCase().startsWith(WebConstants.SFTP_PREFIX)) {
				//point to sftp proxy instead
				directDownloadURL = Uploader.getSftpProxyLink(externalUrl, globalApplicationState, gwt);
			} else {
				directDownloadURL = externalUrl;	
			}
		}
		return directDownloadURL;
	}
}
