package org.sagebionetworks.web.client.widget.licenseddownloader;

import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.FileDownload;
import org.sagebionetworks.web.shared.LicenseAgreement;

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
	private PlaceChanger placeChanger;

	private boolean requireLicenseAcceptance;
	private AsyncCallback<Void> licenseAcceptedCallback;	
	
	@Inject
	public LicensedDownloader(LicensedDownloaderView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.placeChanger = globalApplicationState.getPlaceChanger();
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
	public void configureHeadless(Entity entity, boolean showDownloadLocations) {
		view.setPresenter(this);
		loadDownloadLocations(entity, showDownloadLocations);		
	}
	
	/**
	 * Returns a standard download button
	 * @param entity
	 * @param showDownloadLocations
	 * @return
	 */
	public Widget asWidget(Entity entity, boolean showDownloadLocations) {
		configureHeadless(entity, showDownloadLocations);
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
	public void loadDownloadLocations(final Entity entity, final Boolean showDownloadLocations) {		
		if(entity != null) {
			view.showDownloadsLoading();		
			if(entity instanceof Locationable) {
				Locationable locationable = (Locationable)entity;
				List<LocationData> locations = locationable.getLocations();				
				if(locations != null && locations.size() > 0) {
					this.view.setDownloadLocations(locations, locationable.getMd5());
					
					// show download if requested
					if(showDownloadLocations != null && showDownloadLocations == true) {
						if(downloadAttempted()) {
							showWindow();
						} else {
							view.setUnauthorizedDownloads();
						}
					}			
				} else {
					view.setNoDownloads();
				}
			} else {
				view.setNoDownloads();
			}
		}
	}
			
	// TODO : this is not needed
	@Override
	public void setPlaceChanger(PlaceChanger placeChanger) {
		this.placeChanger = placeChanger;		
	}	
	
	private void setDownloadUnavailable() {
		this.view.setDownloadUrls(null);
		// TODO : more?
	}		

	public boolean downloadAttempted() {
		if(authenticationController.getLoggedInUser() != null) {
			return true;
		} else {
			view.showInfo("Login Required", "Please Login to download data.");
			if(placeChanger != null) {
				placeChanger.goTo(new LoginPlace(DisplayUtils.DEFAULT_PLACE_TOKEN));
			}
		}
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
		} else {
			this.setRequireLicenseAcceptance(false);
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
		this.licenseAcceptedCallback = null;		
	}
	
	public void setRequireLicenseAcceptance(boolean requireLicenseAcceptance) {
		this.requireLicenseAcceptance = requireLicenseAcceptance;
		this.view.setLicenceAcceptanceRequired(requireLicenseAcceptance);
	}
	
	@Deprecated
	public void setLicenseAcceptedCallback(AsyncCallback<Void> callback) {
		this.licenseAcceptedCallback = callback;
	}
	
	@Override
	public void setLicenseAccepted() {		
		// send out to using class to let know of acceptance
		if(licenseAcceptedCallback != null) 
			licenseAcceptedCallback.onSuccess(null);
		// allow the view to skip the license agreement now and show the download view
		setRequireLicenseAcceptance(false);
		showWindow();		
	}

}
