package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.login.LoginModalWidget;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBarViewImpl extends Composite implements FileTitleBarView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private LicensedDownloader licensedDownloader;
	private Md5Link md5Link;
	private FavoriteWidget favoriteWidget;
	private GlobalApplicationState globalAppState;
	private LoginModalWidget loginModalWidget;
	
	@UiField
	HTMLPanel panel;
	@UiField
	HTMLPanel fileFoundContainer;
	@UiField
	HTMLPanel fileNameContainer;
	@UiField
	Anchor licensedDownloadLink;
	@UiField
	Anchor directDownloadLink;
	@UiField
	Anchor authorizedDirectDownloadLink;
	@UiField
	SimplePanel md5LinkContainer;
	@UiField
	Image entityIcon;
	@UiField
	SpanElement fileName;
	@UiField
	SpanElement fileSize;
	@UiField
	SpanElement fileLocation;
	@UiField
	SimplePanel favoritePanel;
	
	interface FileTitleBarViewImplUiBinder extends UiBinder<Widget, FileTitleBarViewImpl> {
	}

	private static FileTitleBarViewImplUiBinder uiBinder = GWT
			.create(FileTitleBarViewImplUiBinder.class);
	
	@Inject
	public FileTitleBarViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			MyEntitiesBrowser myEntitiesBrowser, 
			LicensedDownloader licensedDownloaderHandler,
			FavoriteWidget favoriteWidget,
			GlobalApplicationState globalAppState,
			Md5Link md5Link,
			LoginModalWidget loginRequiredModalWidget) {
		this.iconsImageBundle = iconsImageBundle;
		this.licensedDownloader = licensedDownloaderHandler;
		this.favoriteWidget = favoriteWidget;
		this.md5Link = md5Link;
		this.globalAppState = globalAppState;
		this.loginModalWidget = loginRequiredModalWidget;
		initWidget(uiBinder.createAndBindUi(this));
		md5LinkContainer.addStyleName("inline-block margin-left-5");
		
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		loginRequiredModalWidget.setPrimaryButtonText(DisplayConstants.BUTTON_DOWNLOAD);
		licensedDownloadLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//if there is an href, ignore it
				event.preventDefault();
				licensedDownloader.onDownloadButtonClicked();
			}
		});
		
		ClickHandler authorizedDirectDownloadClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loginModalWidget.showModal();
			}
		};
		authorizedDirectDownloadLink.addClickHandler(authorizedDirectDownloadClickHandler);
	}
	
	private void hideAll() {
		licensedDownloadLink.setVisible(false);
		directDownloadLink.setVisible(false);
		authorizedDirectDownloadLink.setVisible(false);
	}
	
	@Override
	public void createTitlebar(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController) {
		
		hideAll();
		Entity entity = entityBundle.getEntity();

		favoriteWidget.configure(entity.getId());
		
		licensedDownloader.configure(entityBundle);
		
		md5Link.clear();
		md5LinkContainer.clear();
		md5LinkContainer.add(md5Link);

		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(entity, DisplayUtils.IconSize.PX24, iconsImageBundle));
		synapseIconForEntity.applyTo(entityIcon);
		//fileHandle is null if user can't access the filehandle associated with this file entity
		FileHandle fileHandle = DisplayUtils.getFileHandle(entityBundle);
		boolean isFilenamePanelVisible = fileHandle != null;
		fileNameContainer.setVisible(isFilenamePanelVisible);
		if (isFilenamePanelVisible) {
			//don't ask for the size if it's external, just display that this is external data
			if (fileHandle instanceof ExternalFileHandle) {
				fileName.setInnerText(((ExternalFileHandle) fileHandle).getExternalURL());
				fileSize.setInnerText("");
				fileLocation.setInnerText("| External Storage");
			}
			else if (fileHandle instanceof S3FileHandleInterface){
				fileName.setInnerText(fileHandle.getFileName());

				final S3FileHandleInterface s3FileHandle = (S3FileHandleInterface)fileHandle;
				presenter.setS3Description();
				fileSize.setInnerText("| "+DisplayUtils.getFriendlySize(s3FileHandle.getContentSize().doubleValue(), true));

				final String md5 = s3FileHandle.getContentMd5();
				if (md5 != null) {
					md5Link.configure(md5);
				} 
			}
		}
		
		// this allows the menu to respond to the user signing a Terms of Use agreement in the licensed downloader
		licensedDownloader.setEntityUpdatedHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent(event);
			}
		});
		String directDownloadUrl = licensedDownloader.getDirectDownloadURL();
		if (directDownloadUrl != null) {
			//special case, if this starts with sftp proxy, then handle
			String sftpProxy = globalAppState.getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT);
			if (directDownloadUrl.startsWith(sftpProxy)) {
				authorizedDirectDownloadLink.setVisible(true);
				authorizedDirectDownloadLink.setText(entity.getName());
				loginModalWidget.configure(directDownloadUrl, FormPanel.METHOD_POST, FormPanel.ENCODING_MULTIPART);
				String url = ((ExternalFileHandle) fileHandle).getExternalURL();
				presenter.queryForSftpLoginInstructions(url);
			} else {
				directDownloadLink.setVisible(true);
				directDownloadLink.setHref(directDownloadUrl);
				directDownloadLink.setText(entity.getName());
			}
		}
		else {
			licensedDownloadLink.setText(entity.getName());
			licensedDownloadLink.setVisible(true);
		}
	}
	@Override
	public void setLoginInstructions(String instructions) {
		loginModalWidget.setInstructionMessage(instructions);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}

	@Override
	public void setFileLocation(String location) {
		fileLocation.setInnerText(location);
	}
}
