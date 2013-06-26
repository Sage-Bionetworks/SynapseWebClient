package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.AttachmentSelectedEvent;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}

	@UiField
	SimplePanel colLeftPanel;
	@UiField
	SimplePanel colRightPanel;
	@UiField
	SimplePanel fullWidthPanel;
	@UiField
	SimplePanel breadcrumbsPanel;
	@UiField
	SimplePanel actionMenuPanel;

	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private ActionMenu actionMenu;
	private LocationableTitleBar locationableTitleBar;
	private FileTitleBar fileTitleBar;
	private PortalGinInjector ginInjector;
	private EntityTreeBrowser entityTreeBrowser;
	private Breadcrumb breadcrumb;
	private PropertyWidget propertyWidget;
	private LayoutContainer colLeftContainer;
	private LayoutContainer colRightContainer;
	private LayoutContainer fullWidthContainer;
	private Attachments attachmentsPanel;
	private SnapshotWidget snapshotWidget;
	private Long versionNumber;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityMetadata entityMetadata;
	private FilesBrowser filesBrowser;
	private MarkdownWidget markdownWidget;
	private WikiPageWidget wikiPageWidget;
	private PreviewWidget previewWidget;
	
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton,
			ActionMenu actionMenu,
			LocationableTitleBar locationableTitleBar,
			FileTitleBar fileTitleBar,
			EntityTreeBrowser entityTreeBrowser, Breadcrumb breadcrumb,
			PropertyWidget propertyWidget,
			Attachments attachmentsPanel, SnapshotWidget snapshotWidget,
			EntityMetadata entityMetadata, SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector, 
			FilesBrowser filesBrowser, 
			MarkdownWidget markdownWidget, 
			WikiPageWidget wikiPageWidget, 
			PreviewWidget previewWidget) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.actionMenu = actionMenu;
		this.entityTreeBrowser = entityTreeBrowser;
		this.breadcrumb = breadcrumb;
		this.propertyWidget = propertyWidget;
		this.attachmentsPanel = attachmentsPanel;
		this.snapshotWidget = snapshotWidget;
		this.entityMetadata = entityMetadata;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.locationableTitleBar = locationableTitleBar;
		this.fileTitleBar = fileTitleBar;
		this.ginInjector = ginInjector;
		this.filesBrowser = filesBrowser;
		this.previewWidget = previewWidget;
		this.markdownWidget = markdownWidget;	//note that this will be unnecessary after description contents are moved to wiki markdown
		this.wikiPageWidget = wikiPageWidget;
		
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile, String entityTypeDisplay, boolean isAdministrator, boolean canEdit, Long versionNumber) {
		this.versionNumber = versionNumber;
		colLeftContainer = initContainerAndPanel(colLeftContainer, colLeftPanel);
		colRightContainer = initContainerAndPanel(colRightContainer, colRightPanel);
		fullWidthContainer = initContainerAndPanel(fullWidthContainer, fullWidthPanel);

		colLeftContainer.removeAll();
		colRightContainer.removeAll();
		fullWidthContainer.removeAll();

		// add breadcrumbs
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget(bundle.getPath()));

		// setup action menu
		actionMenuPanel.clear();
		actionMenuPanel.add(actionMenu.asWidget(bundle, isAdministrator,
				canEdit, versionNumber));
		
		MarginData widgetMargin = new MarginData(0, 0, 20, 0);

		// Custom layouts for certain entities
		if (bundle.getEntity() instanceof Project) {
			renderProjectEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, widgetMargin);
		} else if (bundle.getEntity() instanceof Folder || bundle.getEntity() instanceof Study || bundle.getEntity() instanceof Analysis) {
			//render Study like a Folder rather than a File (until all of the old types are migrated to the new world of Files and Folders)
			renderFolderEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, widgetMargin);
		} else if (bundle.getEntity() instanceof Summary) {
		    renderSummaryEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, versionNumber, widgetMargin);
		} else {
			// default entity view
			renderFileEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, versionNumber, widgetMargin);
		}
		synapseJSNIUtils.setPageTitle(bundle.getEntity().getName() + " - " + bundle.getEntity().getId());
		synapseJSNIUtils.setPageDescription(bundle.getEntity().getDescription());

		colLeftContainer.layout(true);
		colRightContainer.layout(true);
		fullWidthContainer.layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
		EntityUpdatedHandler handler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		actionMenu.setEntityUpdatedHandler(handler);
		locationableTitleBar.setEntityUpdatedHandler(handler);
		fileTitleBar.setEntityUpdatedHandler(handler);
		filesBrowser.setEntityUpdatedHandler(handler);
		entityMetadata.setEntityUpdatedHandler(handler);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		actionMenu.clearState();
		locationableTitleBar.clearState();
		fileTitleBar.clearState();
		if (colLeftContainer != null)
			colLeftContainer.removeAll();
		if (colRightContainer != null)
			colRightContainer.removeAll();
		if (fullWidthContainer != null)
			fullWidthContainer.removeAll();
	}


	/*
	 * Private Methods
	 */
	// Render the File entity	
	private void renderFileEntity(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit, Long versionNumber, MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		if (bundle.getEntity() instanceof FileEntity)
			colLeftContainer.add(fileTitleBar.asWidget(bundle, isAdmin, canEdit), new MarginData(0, 0, 0, 0));
		else
			colLeftContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit), new MarginData(0, 0, 0, 0));
		entityMetadata.setEntityBundle(bundle, versionNumber);
		colLeftContainer.add(entityMetadata.asWidget(), widgetMargin);
		
		// ** RIGHT **
		// Programmatic Clients
		colRightContainer.add(createProgrammaticClientsWidget(bundle, versionNumber));

		// ** FULL WIDTH
		// Description
		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false), widgetMargin);
		// Wiki
		addWikiPageWidget(fullWidthContainer, bundle, canEdit, 24);
		// Preview
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
			fullWidthContainer.add(getFilePreview(bundle));
		}
		// Provenance Widget for anything other than projects of folders
		if(!(bundle.getEntity() instanceof Project || bundle.getEntity() instanceof Folder)) 
			fullWidthContainer.add(createProvenanceWidget(bundle), widgetMargin);
		// Annotation Editor widget
		fullWidthContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		fullWidthContainer.add(createAttachmentsWidget(bundle, canEdit, false), widgetMargin);
		
		//these types should not have children to show (and don't show deprecated Preview child object)
		
		// ************************************************************************************************		
	}
	
	private Widget getFilePreview(EntityBundle bundle) {
		previewWidget.configure(bundle);
		Widget preview = previewWidget.asWidget();
		preview.addStyleName("span-17 notopmargin padding-top-15");		
		return preview;
	}
	
	// Render the Folder entity
	private void renderFolderEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit,
			MarginData widgetMargin) {
		// ** LEFT **
		entityMetadata.setEntityBundle(bundle, versionNumber);
		fullWidthContainer.add(entityMetadata.asWidget(), new MarginData(0));
		
		// ** RIGHT **
		// none

		// ** FULL WIDTH **
		//SWC-668: render (from top to bottom) description, wiki, then file browser, to make consistent with Project view.
		// Description
		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false), widgetMargin);

		addWikiPageWidget(fullWidthContainer, bundle, canEdit, 24);

		// Child Browser
		fullWidthContainer.add(createEntityFilesBrowserWidget(bundle.getEntity(), false, canEdit));

		LayoutContainer threeCol = new LayoutContainer();
		threeCol.addStyleName("span-24 notopmargin");
		// Annotation widget
		LayoutContainer annotContainer = createPropertyWidget(bundle);
		annotContainer.addStyleName("span-7 notopmargin");
		threeCol.add(annotContainer, widgetMargin);		
		threeCol.add(createSpacer(), widgetMargin);
		fullWidthContainer.add(threeCol, widgetMargin);
	}

	// Render the Project entity
	private void renderProjectEntity(final EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, final boolean canEdit,
			MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		fullWidthContainer.add(entityMetadata.asWidget(), widgetMargin);

		// ** RIGHT **
		//none
	
		// ** FULL WIDTH **
		// Description
		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true), widgetMargin);

		addWikiPageWidget(fullWidthContainer, bundle, canEdit, 24);
			
		// Child File Browser
		fullWidthContainer.add(createEntityFilesBrowserWidget(bundle.getEntity(), true, canEdit));

		LayoutContainer threeCol = new LayoutContainer();
		threeCol.addStyleName("span-24 notopmargin");
		// Annotation widget
		LayoutContainer annotContainer = createPropertyWidget(bundle);
		annotContainer.addStyleName("span-7 notopmargin");
		threeCol.add(annotContainer);		
		threeCol.add(createSpacer());
		// ***** TODO : BOTH OF THESE SHOULD BE REPLACED BY THE NEW ATTACHMENT/MARKDOWN SYSTEM ************		
		// Attachments
		Widget attachContainer = createAttachmentsWidget(bundle, canEdit, false);
		attachContainer.addStyleName("span-7 notopmargin");
		threeCol.add(attachContainer);
		threeCol.add(createSpacer());
		// ************************************************************************************************
		fullWidthContainer.add(threeCol, widgetMargin);
	}

	private void addWikiPageWidget(LayoutContainer container, EntityBundle bundle, boolean canEdit, int spanWidth) {
		wikiPageWidget.clear();
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
			// Child Page Browser
			container.add(wikiPageWidget.asWidget());
			wikiPageWidget.configure(new WikiPageKey(bundle.getEntity().getId(), ObjectType.ENTITY.toString(), null), canEdit, new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					presenter.fireEntityUpdatedEvent();
				}
			}, true, spanWidth);
		}
	}

	private LayoutContainer createSpacer() {
		LayoutContainer onewide = new LayoutContainer();
		onewide.setStyleName("span-1 notopmargin");		
		return onewide;
	}

	// Render Snapshot Entity
	// TODO: This rendering should be phased out in favor of a regular wiki page
	private void renderSummaryEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit, Long versionNumber,
			MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		colLeftContainer.add(entityMetadata.asWidget(), widgetMargin);
		// Description
		colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true), widgetMargin);
		
		// ** RIGHT **
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle, canEdit, false), widgetMargin);

		// ** FULL WIDTH **
		// Snapshot entity
		boolean readOnly = versionNumber != null;
		snapshotWidget.setSnapshot((Summary)bundle.getEntity(), canEdit, readOnly);
		fullWidthContainer.add(snapshotWidget.asWidget());
	}

	private Widget createProvenanceWidget(EntityBundle bundle) {
		final LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.addStyleName("span-7 notopmargin right last");
		LayoutContainer topbar = new LayoutContainer();
		HTML html = new HTML(SafeHtmlUtils.fromSafeConstant("<h4>" + DisplayConstants.PROVENANCE + "</h4>"));
		html.addStyleName("floatleft");
		topbar.add(html);
		lc.add(topbar);
		
	    // Create the property body
	    // the headers for properties.
		ProvenanceWidget provenanceWidget = ginInjector.getProvenanceRenderer();						
		
		Map<String,String> configMap = new HashMap<String,String>();
		Long version = bundle.getEntity() instanceof Versionable ? ((Versionable)bundle.getEntity()).getVersionNumber() : null; 
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(bundle.getEntity().getId(), version));
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
	    provenanceWidget.configure(null, configMap);
	    final Widget provViewWidget = provenanceWidget.asWidget(); 
	    final LayoutContainer border = new LayoutContainer();
	    border.addStyleName("span-7 notopmargin");
	    border.setBorders(true);
	    border.add(provViewWidget);

		LayoutContainer menu = new LayoutContainer();		
		menu.addStyleName("floatleft");
		topbar.add(menu, new MarginData(8,0,0,5));
		
	    lc.add(border);
	    lc.layout();
		return lc;
	}

	private Widget createProgrammaticClientsWidget(EntityBundle bundle, Long versionNumber) {		
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-7 notopmargin");
		lc.setAutoHeight(true);
		LayoutContainer pcc = ProgrammaticClientCode.createLoadWidget(bundle.getEntity().getId(), versionNumber, synapseJSNIUtils, sageImageBundle);
		pcc.addStyleName("right");
		lc.add(pcc);
		lc.layout();
	    return lc;
	}

	private Widget createEntityFilesBrowserWidget(Entity entity, boolean showTitle, boolean canEdit) {
		filesBrowser.setCanEdit(canEdit);
		if(showTitle) 
			filesBrowser.configure(entity.getId(), DisplayConstants.FILES);
		else 
			filesBrowser.configure(entity.getId());
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("left");
		lc.setStyleAttribute("margin", "0px 0px 20px 0px");
		lc.add(filesBrowser.asWidget());
		return lc;
	}
	
	private Widget createDescriptionWidget(final EntityBundle bundle, String entityTypeDisplay, boolean showWhenEmpty) {
		final LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		String description = bundle.getEntity().getDescription();
	
		if(!showWhenEmpty) {
			if(description == null || "".equals(description))
				return lc;
		}
		
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"clear: left;\"></div>")));
	
		// Add the description body
	    if(description != null && !("".equals(description))) {
	    	if (!DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
				lc.add(markdownWidget);
		    		markdownWidget.setMarkdown(description, new WikiPageKey(bundle.getEntity().getId(),  ObjectType.ENTITY.toString(), null), false, false);
	    	}
	    	else {
	    		Label plainDescriptionText = new Label();
	    		plainDescriptionText.setWidth("100%");
	    		plainDescriptionText.addStyleName("wiki-description");
	    		plainDescriptionText.setText(description);
	    		lc.add(plainDescriptionText);
	    	}
	    }
	    
   		return lc;
	}
	
	private LayoutContainer createPropertyWidget(EntityBundle bundle) {
	    // Create the property body
	    // the headers for properties.
	    propertyWidget.setEntityBundle(bundle);
	    LayoutContainer lc = new LayoutContainer();
	    lc.addStyleName("span-7");
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		if (!propertyWidget.isEmpty()) {
			lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h4>" + DisplayConstants.ANNOTATIONS + "</h4>")));
		    // Create the property body
		    // the headers for properties.
		    lc.add(propertyWidget.asWidget());
		}
		lc.layout();
		return lc;
	}

	private Widget createAttachmentsWidget(final EntityBundle bundle, boolean canEdit, boolean showWhenEmpty) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
        c.setLayout(layout);

        c.add(new Html("<h4>Attachments</h4>"), new HBoxLayoutData(new Margins(0, 5, 0, 0)));
        HBoxLayoutData flex = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        flex.setFlex(1);

        final String baseURl = GWT.getModuleBaseURL()+"attachment";
        final String actionUrl =  baseURl+ "?" + WebConstants.ENTITY_PARAM_KEY + "=" + bundle.getEntity().getId() ;

        if(canEdit) {
	        Anchor addBtn = new Anchor();
	        addBtn.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.add16()));
	        addBtn.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					AddAttachmentDialog.showAddAttachmentDialog(actionUrl,sageImageBundle,DisplayConstants.ATTACHMENT_DIALOG_WINDOW_TITLE, DisplayConstants.ATTACHMENT_DIALOG_BUTTON_TEXT,new AddAttachmentDialog.Callback() {
						@Override
						public void onSaveAttachment(UploadResult result) {
							if(result != null){
								if(UploadStatus.SUCCESS == result.getUploadStatus()){
									showInfo(DisplayConstants.TEXT_ATTACHMENT_SUCCESS, "");
								}else{
									showErrorMessage(DisplayConstants.ERRROR_ATTACHMENT_FAILED+result.getMessage());
								}
							}
							presenter.fireEntityUpdatedEvent();
						}
					});
				}
			});
	        c.add(addBtn, new HBoxLayoutData(new Margins(0)));
        }
        lc.add(c);

        // We just create a new one each time.
        attachmentsPanel.configure(baseURl, bundle.getEntity(), false);
        if(!showWhenEmpty && attachmentsPanel.isEmpty()) 
        	return new LayoutContainer();
        attachmentsPanel.clearHandlers();
        attachmentsPanel.addAttachmentSelectedHandler(new AttachmentSelectedHandler() {
			
			@Override
			public void onAttachmentSelected(AttachmentSelectedEvent event) {
				String url = DisplayUtils.createAttachmentUrl(baseURl, bundle.getEntity().getId(), event.getTokenId(), event.getTokenId());
				DisplayUtils.newWindow(url, "", "");
			}
		});
        		
        lc.add(attachmentsPanel.asWidget());
		lc.layout();
		return lc;
	}

	private LayoutContainer initContainerAndPanel(LayoutContainer container,
			SimplePanel panel) {
		if(container == null) {
			container = new LayoutContainer();
			container.setAutoHeight(true);
			container.setAutoWidth(true);
			panel.clear();
			panel.add(container);
}
		return container;
	}


}
