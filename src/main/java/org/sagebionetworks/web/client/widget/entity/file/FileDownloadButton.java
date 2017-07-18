package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.aws.AwsSdk;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileDownloadButton implements FileDownloadButtonView.Presenter, SynapseWidgetPresenter {
	
	public static final String ACCESS_REQUIREMENTS_LINK = "#!AccessRequirements:ID=";
	public static final String LOGIN_PLACE_LINK = "#!LoginPlace:0";
	private FileDownloadButtonView view;
	private EntityBundle entityBundle;
	private EntityUpdatedHandler entityUpdatedHandler;
	private SynapseClientAsync synapseClient;
	private LoginModalWidget loginModalWidget;
	private GlobalApplicationState globalAppState;
	private SynapseAlert synAlert;
	private PortalGinInjector ginInjector;
	DataAccessClientAsync dataAccessClient;
	AuthenticationController authController;
	SynapseJSNIUtils jsniUtils;
	GWTWrapper gwt;
	CookieProvider cookies;
	boolean isHidingClientHelp = false;
	AwsSdk awsSdk;
	PopupUtilsView popupUtilsView;
	FileHandle dataFileHandle;
	
	@Inject
	public FileDownloadButton(FileDownloadButtonView view, 
			SynapseClientAsync synapseClient, 
			LoginModalWidget loginModalWidget,
			GlobalApplicationState globalAppState,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			DataAccessClientAsync dataAccessClient,
			AuthenticationController authController,
			SynapseJSNIUtils jsniUtils,
			GWTWrapper gwt,
			CookieProvider cookies,
			AwsSdk awsSdk,
			PopupUtilsView popupUtilsView) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.loginModalWidget = loginModalWidget;
		this.globalAppState = globalAppState;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		this.authController = authController;
		this.jsniUtils = jsniUtils;
		this.gwt = gwt;
		this.cookies = cookies;
		this.awsSdk = awsSdk;
		this.popupUtilsView = popupUtilsView;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		loginModalWidget.setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
	}
	
	public void configure(final EntityBundle bundle) {
		view.clear();
		dataAccessClient.getRestrictionInformation(bundle.getEntity().getId(), RestrictableObjectType.ENTITY, new AsyncCallback<RestrictionInformationResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			public void onSuccess(RestrictionInformationResponse restrictionInformation) {
				configure(bundle, restrictionInformation);
			};
		});
	}
	
	public void configure(EntityBundle bundle, RestrictionInformationResponse restrictionInformation) {
		view.clear();
		this.entityBundle = bundle;
		view.setClientsHelpVisible(false);
		dataFileHandle = null;
		
		if (!authController.isLoggedIn()) {
			view.setDirectDownloadLink(LOGIN_PLACE_LINK);
			view.setDirectDownloadLinkVisible(true);
		} else if (restrictionInformation.getHasUnmetAccessRequirement()) {
			// if in alpha, send to access requirements
			view.setDirectDownloadLink(ACCESS_REQUIREMENTS_LINK+bundle.getEntity().getId());
			view.setDirectDownloadLinkVisible(true);
		} else {
			dataFileHandle = getFileHandle();
			if (dataFileHandle != null) {
				if (dataFileHandle instanceof ExternalObjectStoreFileHandle) {
					view.setLicensedDownloadLinkVisible(true);
				} else {
					String fileNameOverride = entityBundle.getFileName();
					String directDownloadUrl = getDirectDownloadURL((FileEntity)entityBundle.getEntity(), dataFileHandle, fileNameOverride);
					
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
						if (!isHidingClientHelp) {
							view.setClientsHelpVisible(true);	
						}
					}
				}
			}
		}
		
		FileClientsHelp clientsHelp = ginInjector.getFileClientsHelp();
		view.setFileClientsHelp(clientsHelp.asWidget());
		clientsHelp.configure(entityBundle.getEntity().getId());
	}
	
	public FileHandle getFileHandle() {
		if(entityBundle != null && entityBundle.getEntity() != null) {
			if (entityBundle.getEntity() instanceof FileEntity) {
				if (authController.isLoggedIn()) {
					return DisplayUtils.getFileHandle(entityBundle);
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
	
	public void hideClientHelp() {
		isHidingClientHelp = true;
		view.setClientsHelpVisible(false);
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
		//ask for credentials, use bucket/endpoint info from storage location
		ExternalObjectStoreFileHandle objectStoreFileHandle = (ExternalObjectStoreFileHandle) dataFileHandle;
		view.showS3DirectLoginDialog(objectStoreFileHandle.getEndpointUrl());
	}
	
	@Override
	public void onS3DirectDownloadClicked(String accessKeyId, String secretAccessKey) {
		final ExternalObjectStoreFileHandle objectStoreFileHandle = (ExternalObjectStoreFileHandle) dataFileHandle;
		CallbackP<JavaScriptObject> s3Callback = new CallbackP<JavaScriptObject>() {
			@Override
			public void invoke(JavaScriptObject s3) {
				String presignedUrl = awsSdk.getPresignedURL(objectStoreFileHandle.getFileKey(), objectStoreFileHandle.getBucket(), s3);
				popupUtilsView.openInNewWindow(presignedUrl);
			}
		};
		awsSdk.getS3(accessKeyId, secretAccessKey, objectStoreFileHandle.getBucket(), objectStoreFileHandle.getEndpointUrl(), s3Callback);	
	}
	
	@Override
	public void onAuthorizedDirectDownloadClicked() {
		loginModalWidget.showModal();
	}
	
	public void setSize(ButtonSize size) {
		view.setButtonSize(size);
	}
}
