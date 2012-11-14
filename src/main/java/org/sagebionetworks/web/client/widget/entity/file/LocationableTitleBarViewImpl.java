package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.LocationTypeNames;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.CancelEvent;
import org.sagebionetworks.web.client.events.CancelHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.browse.MyEntitiesBrowser;
import org.sagebionetworks.web.client.widget.entity.download.LocationableUploader;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.EntityType;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocationableTitleBarViewImpl extends HorizontalPanel implements LocationableTitleBarView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private LocationableUploader locationableUploader;
	private LicensedDownloader licensedDownloader;
	private Widget downloadButton = null;
	
	private SynapseJSNIUtils synapseJSNIUtils;
	@Inject
	public LocationableTitleBarViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, 
			AccessMenuButton accessMenuButton,
			AccessControlListEditor accessControlListEditor,
			LocationableUploader locationableUploader, 
			MyEntitiesBrowser myEntitiesBrowser, 
			LicensedDownloader licensedDownloader, 
			EntityTypeProvider typeProvider,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.locationableUploader = locationableUploader;
		this.licensedDownloader = licensedDownloader;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.setTableHeight("30px");
	}
	public static String getLocationablePath(EntityBundle bundle) {
		String locationPath = null;
		if (!(bundle.getEntity() instanceof Locationable))
			throw new IllegalArgumentException("Bundle must reference a Locationable entity");
		if (((Locationable)bundle.getEntity()).getLocations() != null && ((Locationable)bundle.getEntity()).getLocations().size() > 0)
			locationPath = ((Locationable)bundle.getEntity()).getLocations().get(0).getPath();
		return locationPath;
	}
	
	public static boolean isDataPossiblyWithinLocationable(EntityBundle bundle, boolean isLoggedIn) {
		if (!(bundle.getEntity() instanceof Locationable))
			throw new IllegalArgumentException("Bundle must reference a Locationable entity");
		
		boolean hasData = false;
		//if user isn't logged in, then there might be something here
		if (!isLoggedIn)
			hasData = true;
		//if it has unmet access requirements, then there might be something here
		else if (bundle.getUnmetAccessRequirements() != null && bundle.getUnmetAccessRequirements().size() > 0)
			hasData = true;
		//else, if it has a locations list whose size is > 0
		else if (((Locationable)bundle.getEntity()).getLocations() != null && ((Locationable)bundle.getEntity()).getLocations().size() > 0)
			hasData = true;
		return hasData;
	}
	
	@Override
	public void createMenu(
			EntityBundle entityBundle, 
			EntityType entityType, 
			AuthenticationController authenticationController,
			boolean isAdministrator,
			boolean canEdit, 
			boolean readOnly) {
		
		Entity entity = entityBundle.getEntity();

		UserSessionData sessionData = authenticationController.getLoggedInUser();
		UserProfile userProfile = (sessionData==null ? null : sessionData.getProfile());

		if(downloadButton == null){
			downloadButton = licensedDownloader.asWidget(entityBundle, userProfile);
			DisplayUtils.addTooltip(this.synapseJSNIUtils, downloadButton, DisplayConstants.BUTTON_DOWNLOAD, TOOLTIP_POSITION.BOTTOM);
		}
		
		if (entity instanceof Locationable) {
			
			//configure this view based on if this entity has locations to download
			Locationable locationable = (Locationable)entity;
			
			if (isDataPossiblyWithinLocationable(entityBundle, authenticationController.isLoggedIn())) {
				HorizontalPanel panel = new HorizontalPanel();
				panel.setVerticalAlign(VerticalAlignment.MIDDLE);
				
				//add an anchor with the file name, that redirects to the download button for functionality
				
				Anchor a = new Anchor("<h2 class=\"downloadLink link\">" + entity.getName() + " ("+entity.getId()+")</h2>", true);
				a.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						downloadButton.fireEvent(event);
					}
				});
				DisplayUtils.addTooltip(this.synapseJSNIUtils, a, DisplayConstants.BUTTON_DOWNLOAD, TOOLTIP_POSITION.BOTTOM);
				
				panel.add(downloadButton);
				panel.add(a);
				final SimplePanel sizePanel = new SimplePanel();
				
				if (locationable.getLocations() != null && locationable.getLocations().size() > 0) {
					LocationData locationData = locationable.getLocations().get(0);
					LocationTypeNames locationTypeName = locationData.getType();
					//don't ask for the size if it's external, just display that this is external data
					boolean isExternal = locationTypeName == LocationTypeNames.external;
					if (isExternal) {
						SafeHtmlBuilder shb = new SafeHtmlBuilder();
						shb.appendHtmlConstant("<span style=\"margin-left: 10px;\" class=\"file-size\">(External Storage)</span>");
						HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
						sizePanel.add( htmlPanel);
					}
					else {
						presenter.updateNodeStorageUsage(new AsyncCallback<Long>() {
							@Override
							public void onSuccess(Long result) {
								SafeHtmlBuilder shb = new SafeHtmlBuilder();
								shb.appendHtmlConstant("<span style=\"margin-left: 10px;\" class=\"file-size\">(");
								shb.appendEscaped(DisplayUtils.getFriendlySize(result.doubleValue(), true));
								shb.appendHtmlConstant(" - Synapse Storage)</span>");
								HTMLPanel htmlPanel = new HTMLPanel(shb.toSafeHtml());
								sizePanel.add(htmlPanel);
							}
							
							@Override
							public void onFailure(Throwable caught) {
								//could not determine the size, leave it blank.
							}
						});
					}
						
					panel.add(sizePanel);
					if (!isExternal) {
						//also add md5 if not external
						a = new Anchor("<span style=\"margin-left: 10px;\" class=\"file-md5\">md5</span>", true);
						final String md5 = locationable.getMd5();
						a.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								showMd5Dialog(md5);
							}
						});
						DisplayUtils.addTooltip(synapseJSNIUtils, a, md5, TOOLTIP_POSITION.RIGHT);
						panel.add(a);
					}
				}
				this.add(panel);
			}
			else {
				SimplePanel panel = new SimplePanel();
				panel.addStyleName("span-17");
				panel.addStyleName("bordered-form");
				panel.addStyleName("notopmargin");
				VerticalPanel vPanel = new VerticalPanel();
				panel.add(vPanel);
				HorizontalPanel topRow = new HorizontalPanel();
				String colon = canEdit ? ":" : "";
				topRow.add(new Html("<h3 style=\"margin-right: 10px;\">"+DisplayConstants.LOCATIONABLE_NO_FILE_FOUND + colon+"</h3>"));
				if (canEdit)
					topRow.add(getUploadButton(entityBundle, entityType));
				HTMLPanel bottomRow = new HTMLPanel(DisplayConstants.LOCATIONABLE_NO_FILE_FOUND_DETAIL);
				vPanel.add(topRow);
				vPanel.add(bottomRow);
				this.add(panel);
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
		this.removeAll();
	}

	/**
	 * 'Upload File' button
	 * @param entity 
	 * @param entityType 
	 */
	private Widget getUploadButton(final EntityBundle entityBundle, EntityType entityType) {
		Button uploadButton = new Button(DisplayConstants.TEXT_UPLOAD_FILE, AbstractImagePrototype.create(iconsImageBundle.NavigateUpColor16()));
		uploadButton.setHeight(25);
		final Window window = initializeLocationableUploaderWindow();
		
		uploadButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				window.removeAll();
				window.setSize(400, 320);
				window.setPlain(true);
				window.setModal(true);		
				window.setBlinkModal(true);
				window.setHeading(DisplayConstants.TEXT_UPLOAD_FILE);
				window.setLayout(new FitLayout());			
				window.add(locationableUploader.asWidget(entityBundle), new MarginData(5));
				window.show();
			}
		});
		return uploadButton;
	}
	
	private Window initializeLocationableUploaderWindow() {
		final Window window = new Window();  
		locationableUploader.clearHandlers();
		locationableUploader.addPersistSuccessHandler(new EntityUpdatedHandler() {				
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				window.hide();
				presenter.fireEntityUpdatedEvent();
			}
		});
		locationableUploader.addCancelHandler(new CancelHandler() {				
			@Override
			public void onCancel(CancelEvent event) {
				window.hide();
			}
		});
		return window;
	}
	
	private void showMd5Dialog(String md5) {
		final Dialog window = new Dialog();
		window.setSize(220, 85);
		window.setPlain(true);
		window.setModal(true);
		window.setBlinkModal(true);
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
