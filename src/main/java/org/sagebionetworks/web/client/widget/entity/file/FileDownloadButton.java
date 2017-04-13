package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictionInformation;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileDownloadButton implements FileDownloadButtonView.Presenter, SynapseWidgetPresenter {
	
	private FileDownloadButtonView view;
	private EntityBundle entityBundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private LicensedDownloader licensedDownloader;
	private LoginModalWidget loginModalWidget;
	private GlobalApplicationState globalAppState;
	private SynapseAlert synAlert;
	private PortalGinInjector ginInjector;
	DataAccessClientAsync dataAccessClient;
	AuthenticationController authController;
	SynapseJSNIUtils jsniUtils;
	GWTWrapper gwt;
	CookieProvider cookies;
	
	@Inject
	public FileDownloadButton(FileDownloadButtonView view, 
			SynapseClientAsync synapseClient, 
			LicensedDownloader licensedDownloader, 
			LoginModalWidget loginModalWidget,
			GlobalApplicationState globalAppState,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			DataAccessClientAsync dataAccessClient,
			AuthenticationController authController,
			SynapseJSNIUtils jsniUtils,
			GWTWrapper gwt,
			CookieProvider cookies) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.licensedDownloader = licensedDownloader;
		this.loginModalWidget = loginModalWidget;
		this.globalAppState = globalAppState;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		this.authController = authController;
		this.jsniUtils = jsniUtils;
		this.gwt = gwt;
		this.cookies = cookies;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		licensedDownloader.setEntityUpdatedHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				fireEntityUpdatedEvent(event);
			}
		});
		loginModalWidget.setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
	}
	
	public void configure(final EntityBundle bundle) {
		view.clear();
		dataAccessClient.getRestrictionInformation(bundle.getEntity().getId(), new AsyncCallback<RestrictionInformation>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			public void onSuccess(RestrictionInformation restrictionInformation) {
				configure(bundle, restrictionInformation);
			};
		});
	}
	
	public void configure(EntityBundle bundle, RestrictionInformation restrictionInformation) {
		view.clear();
		this.entityBundle = bundle;
		view.setClientsHelpVisible(false);
		
		if (!authController.isLoggedIn()) {
			view.setDirectDownloadLink("#!LoginPlace:0");
			view.setDirectDownloadLinkVisible(true);
		} else if (restrictionInformation.getHasUnmetAccessRequirement()) {
			// if in alpha, send to access requirements
			if (DisplayUtils.isInTestWebsite(cookies)) {
				view.setDirectDownloadLink("#!AccessRequirements:ID="+bundle.getEntity().getId());
				view.setDirectDownloadLinkVisible(true);
			} else {
				// else, use licensed downloader
				licensedDownloader.configure(entityBundle);
				view.setLicensedDownloadLinkVisible(true);
			}
		} else {
			String directDownloadUrl = getDirectDownloadUrl();
			//special case, if this starts with sftp proxy, then handle
			String sftpProxy = globalAppState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT);
			if (directDownloadUrl.startsWith(sftpProxy)) {
				view.setAuthorizedDirectDownloadLinkVisible(true);
				loginModalWidget.configure(directDownloadUrl, FormPanel.METHOD_POST, FormPanel.ENCODING_MULTIPART);
				FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
				String url = ((ExternalFileHandle) fileHandle).getExternalURL();
				queryForSftpLoginInstructions(url);
			} else {
				view.setDirectDownloadLink(directDownloadUrl);
				view.setDirectDownloadLinkVisible(true);
				view.setClientsHelpVisible(true);
			}
		}
		
		FileClientsHelp clientsHelp = ginInjector.getFileClientsHelp();
		view.setFileClientsHelp(clientsHelp.asWidget());
		clientsHelp.configure(entityBundle.getEntity().getId());
	}
	
	public String getDirectDownloadUrl() {
		if(entityBundle != null && entityBundle.getEntity() != null) {
			if (entityBundle.getEntity() instanceof FileEntity) {
				FileEntity fileEntity = (FileEntity)entityBundle.getEntity();
				if (authController.isLoggedIn()) {
					FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
					if (fileHandle != null) {
						String fileNameOverride = entityBundle.getFileName();
						return getDirectDownloadURL(fileEntity, fileHandle, fileNameOverride);
					}
				}
			}
		}
		return null;
	}
	
	public String getDirectDownloadURL(FileEntity fileEntity, FileHandle fileHandle, String fileNameOverride) {
		String externalUrl = null;
		if (fileHandle instanceof ExternalFileHandle) {
			externalUrl = ((ExternalFileHandle) fileHandle).getExternalURL();
		}
		
		String directDownloadURL = null;
		if (externalUrl == null)
			directDownloadURL = DisplayUtils.createFileEntityUrl(jsniUtils.getBaseFileHandleUrl(), fileEntity.getId(), fileEntity.getVersionNumber(), false, authController.getCurrentXsrfToken());
		else {
			if (externalUrl.toLowerCase().startsWith(WebConstants.SFTP_PREFIX)) {
				//point to sftp proxy instead
				directDownloadURL = Uploader.getSftpProxyLink(fileNameOverride, externalUrl, globalAppState, gwt);
			} else {
				directDownloadURL = externalUrl;	
			}
		}
		return directDownloadURL;
	}
	
	public void setClientsHelpVisible(boolean visible) {
		view.setClientsHelpVisible(visible);
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
    
	public void fireEntityUpdatedEvent(EntityUpdatedEvent event) {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(event);
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}
	
	public void queryForSftpLoginInstructions(String url) {
		synapseClient.getHost(url, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String host) {
				//update the download login dialog message
				loginModalWidget.setInstructionMessage(DisplayConstants.DOWNLOAD_CREDENTIALS_REQUIRED + SafeHtmlUtils.htmlEscape(host));
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void onLicensedDownloadClick() {
		licensedDownloader.onDownloadButtonClicked();
	}
	
	@Override
	public void onAuthorizedDirectDownloadClicked() {
		loginModalWidget.showModal();
	}
	
	public void setSize(ButtonSize size) {
		view.setButtonSize(size);
	}
}
