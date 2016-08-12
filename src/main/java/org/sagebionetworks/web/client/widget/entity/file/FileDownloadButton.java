package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
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
	
	@Inject
	public FileDownloadButton(FileDownloadButtonView view, 
			SynapseClientAsync synapseClient, 
			LicensedDownloader licensedDownloader, 
			LoginModalWidget loginModalWidget,
			GlobalApplicationState globalAppState,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.licensedDownloader = licensedDownloader;
		this.loginModalWidget = loginModalWidget;
		this.globalAppState = globalAppState;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
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
	
	public void configure(EntityBundle bundle) {
		view.clear();
		view.setPresenter(this);
		this.entityBundle = bundle;
		licensedDownloader.configure(entityBundle);
		String directDownloadUrl = licensedDownloader.getDirectDownloadURL();
		view.setClientsHelpVisible(false);
		if (directDownloadUrl != null) {
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
		else {
			view.setLicensedDownloadLinkVisible(true);
		}
		
		FileClientsHelp clientsHelp = ginInjector.getFileClientsHelp();
		view.setFileClientsHelp(clientsHelp.asWidget());
		clientsHelp.configure(entityBundle.getEntity().getId());
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
