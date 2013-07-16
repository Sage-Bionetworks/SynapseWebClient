package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.LocationTypeNames;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableTitleBarViewImpl extends Composite implements LocationableTitleBarView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private Uploader locationableUploader;
	private LicensedDownloader licensedDownloader;
	private Widget downloadButton = null;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Anchor md5Link;
	private FavoriteWidget favoriteWidget;	
	AuthenticationController authenticationController;
	NodeModelCreator nodeModelCreator;
	
	@UiField
	HTMLPanel panel;
	@UiField
	HTMLPanel fileFoundContainer;
	@UiField
	HTMLPanel noFileFoundContainer;
	@UiField
	HTMLPanel fileNameContainer;
	@UiField
	Anchor entityLink;
	@UiField
	SimplePanel md5LinkContainer;
	@UiField
	SimplePanel downloadButtonContainer;
	@UiField
	SimplePanel uploadButtonContainer;
	@UiField
	SpanElement entityId;
	@UiField
	Image entityIcon;
	@UiField
	SpanElement fileName;
	@UiField
	SpanElement fileSize;
	@UiField
	SimplePanel favoritePanel;

	private HandlerRegistration entityLinkHandlerRegistration;
	
	interface LocationableTitleBarViewImplUiBinder extends UiBinder<Widget, LocationableTitleBarViewImpl> {
	}

	private static LocationableTitleBarViewImplUiBinder uiBinder = GWT
			.create(LocationableTitleBarViewImplUiBinder.class);
	
	@Inject
	public LocationableTitleBarViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor,
			Uploader locationableUploader, 
			MyEntitiesBrowser myEntitiesBrowser, 
			LicensedDownloader licensedDownloader, 
			EntityTypeProvider typeProvider,
			SynapseJSNIUtils synapseJSNIUtils,
			FavoriteWidget favoriteWidget,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator) {
		this.iconsImageBundle = iconsImageBundle;
		this.locationableUploader = locationableUploader;
		this.licensedDownloader = licensedDownloader;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.favoriteWidget = favoriteWidget;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		
		initWidget(uiBinder.createAndBindUi(this));
		downloadButtonContainer.addStyleName("inline-block margin-left-5");
		md5LinkContainer.addStyleName("inline-block font-italic margin-left-5");
		entityLink.addStyleName("downloadLink link");
		uploadButtonContainer.addStyleName("inline-block vertical-align-bottom");
		
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
	}
	public static String getLocationablePath(EntityBundle bundle) {
		String locationPath = null;
		if (!(bundle.getEntity() instanceof Locationable))
			throw new IllegalArgumentException("Bundle must reference a Locationable entity");
		if (((Locationable)bundle.getEntity()).getLocations() != null && ((Locationable)bundle.getEntity()).getLocations().size() > 0)
			locationPath = ((Locationable)bundle.getEntity()).getLocations().get(0).getPath();
		return locationPath;
	}
	
	@Override
	public void createTitlebar(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			boolean isAdministrator,
			boolean canEdit) {
		
		Entity entity = entityBundle.getEntity();

		favoriteWidget.configure(entity.getId());
		
		UserSessionData sessionData = authenticationController.getCurrentUserSessionData();
		UserProfile userProfile = (sessionData==null ? null : sessionData.getProfile());
		
		downloadButton = licensedDownloader.asWidget(entityBundle, userProfile);
		DisplayUtils.addTooltip(this.synapseJSNIUtils, downloadButton, DisplayConstants.BUTTON_DOWNLOAD, TOOLTIP_POSITION.BOTTOM);
		downloadButtonContainer.clear();
		downloadButtonContainer.add(downloadButton);
		
		md5Link = new Anchor("md5");
		md5LinkContainer.clear();
		md5LinkContainer.add(md5Link);
		
		if (entity instanceof Locationable) {
			//configure this view based on if this entity has locations to download
			Locationable locationable = (Locationable)entity;
			boolean isDataPossiblyWithinLocationable = LocationableTitleBar.isDataPossiblyWithinLocationable(entityBundle, authenticationController.isLoggedIn());
			noFileFoundContainer.setVisible(!isDataPossiblyWithinLocationable);
			fileFoundContainer.setVisible(isDataPossiblyWithinLocationable);
			if (isDataPossiblyWithinLocationable) {
				//add an anchor with the file name, that redirects to the download button for functionality
				entityLink.setText(entity.getName());
				entityId.setInnerText(entity.getId());
				AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(entity, DisplayUtils.IconSize.PX24, iconsImageBundle));
				synapseIconForEntity.applyTo(entityIcon);
				
				boolean isFilenamePanelVisible = locationable.getLocations() != null && locationable.getLocations().size() > 0;
				fileNameContainer.setVisible(isFilenamePanelVisible);
				if (isFilenamePanelVisible) {
					//if entity name is not shown, we might have a locationable filename to show
					String locationPath = LocationableTitleBarViewImpl.getLocationablePath(entityBundle);
					fileName.setInnerText(locationPath != null ? DisplayUtils.getFileNameFromExternalUrl(locationPath) : "");
					LocationData locationData = locationable.getLocations().get(0);
					LocationTypeNames locationTypeName = locationData.getType();
					//don't ask for the size if it's external, just display that this is external data
					boolean isExternal = locationTypeName == LocationTypeNames.external;
					if (isExternal) {
						fileSize.setInnerText("(External Storage)");
					}
					else {
						fileSize.setInnerText("");
					}
					md5Link.setVisible(!isExternal);
					if (!isExternal) {
						//also add md5 if not external
						final String md5 = locationable.getMd5();
						md5Link.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								showMd5Dialog(md5);
							}
						});
						DisplayUtils.addTooltip(synapseJSNIUtils, md5Link, md5, TOOLTIP_POSITION.BOTTOM);
					}
				}
			}
			else {
				uploadButtonContainer.clear();
				if (canEdit)
					uploadButtonContainer.add(DisplayUtils.getUploadButton(entityBundle, entityType, locationableUploader, iconsImageBundle, new EntityUpdatedHandler() {				
						@Override
						public void onPersistSuccess(EntityUpdatedEvent event) {
							presenter.fireEntityUpdatedEvent();
						}
					}));
			}
		}
		
		// Configure the button
		licensedDownloader.configureHeadless(entityBundle, userProfile);
		// this allows the menu to respond to the user signing a Terms of Use agreement in the licensed downloader
		licensedDownloader.clearHandlers();
		licensedDownloader.addEntityUpdatedHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		});
		String directDownloadUrl = licensedDownloader.getDirectDownloadURL();
		if (directDownloadUrl != null) {
			//clear old handler, if there is one
			if (entityLinkHandlerRegistration != null) {
				entityLinkHandlerRegistration.removeHandler();
				entityLinkHandlerRegistration = null;
			}
			entityLink.setHref(directDownloadUrl);
			entityLink.setTarget("_blank");
		}
		else {
			//clear href, if there is one
			entityLink.setHref(null);
			entityLinkHandlerRegistration = entityLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//if there is an href, ignore it
					event.preventDefault();
					downloadButton.fireEvent(event);
				}
			});
		}
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
	
	private void showMd5Dialog(String md5) {
		final Dialog window = new Dialog();
		window.setSize(220, 85);
		window.setPlain(true);
		window.setModal(true);
		window.setHeading("md5");
		
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span style=\"margin-left: 10px;\">"+md5+"</span>");
		HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
		window.add(htmlPanel);
		
	    window.setButtons(Dialog.OK);
	    window.setButtonAlign(HorizontalAlignment.CENTER);
	    window.setHideOnButtonClick(true);
		window.setResizable(false);
		window.show();
	}
}
