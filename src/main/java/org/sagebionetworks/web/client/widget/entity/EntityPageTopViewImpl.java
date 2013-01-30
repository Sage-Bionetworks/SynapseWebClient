package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Page;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.AttachmentSelectedEvent;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Html;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}

	private static final int PROVENANCE_HEIGHT_PX = 250;

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
	private PortalGinInjector ginInjector;
	private EntityTreeBrowser entityTreeBrowser;
	private Breadcrumb breadcrumb;
	private PropertyWidget propertyWidget;
	private LayoutContainer colLeftContainer;
	private LayoutContainer colRightContainer;
	private LayoutContainer fullWidthContainer;
	private Attachments attachmentsPanel;
	private SnapshotWidget snapshotWidget;
	private boolean readOnly = false;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityMetadata entityMetadata;
	private FilesBrowser filesBrowser;	
	private PagesBrowser pagesBrowser;
	private CookieProvider cookies;
	private MarkdownWidget markdownWidget;
	
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton,
			ActionMenu actionMenu,
			LocationableTitleBar locationableTitleBar,
			EntityTreeBrowser entityTreeBrowser, Breadcrumb breadcrumb,
			PropertyWidget propertyWidget,
			Attachments attachmentsPanel, SnapshotWidget snapshotWidget,
			EntityMetadata entityMetadata, SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector, FilesBrowser filesBrowser, PagesBrowser pagesBrowser, CookieProvider cookies, MarkdownWidget markdownWidget) {
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
		this.ginInjector = ginInjector;
		this.filesBrowser = filesBrowser;
		this.pagesBrowser = pagesBrowser;
		this.cookies = cookies;
		this.markdownWidget = markdownWidget;
		
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile, String entityTypeDisplay, boolean isAdministrator, boolean canEdit, boolean readOnly) {
		this.readOnly = readOnly;

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
				canEdit, readOnly));

		MarginData widgetMargin = new MarginData(0, 0, 20, 0);

		// Custom layouts for certain entities
		if (bundle.getEntity() instanceof Project) {
			renderProjectEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, readOnly, widgetMargin);
		} else if (bundle.getEntity() instanceof Folder || bundle.getEntity() instanceof Study) {  //render Study like a Folder rather than a File (until all of the old types are migrated to the new world of Files and Folders)
			renderFolderEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, readOnly, widgetMargin);
		} else if (bundle.getEntity() instanceof Summary) {
		    renderSummaryEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, readOnly, widgetMargin);
		} else if (bundle.getEntity() instanceof Page) {
		    renderPageEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, readOnly, widgetMargin);
		} else {
			// default entity view
			renderFileEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, readOnly, widgetMargin);
		}

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
		actionMenu.addEntityUpdatedHandler(handler);
		locationableTitleBar.addEntityUpdatedHandler(handler);
		filesBrowser.addEntityUpdatedHandler(handler);
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
	private void renderFileEntity(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly, MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		colLeftContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit, readOnly), new MarginData(0, 0, 0, 0));
		entityMetadata.setEntityBundle(bundle, readOnly);
		colLeftContainer.add(entityMetadata.asWidget(), widgetMargin);
//		colLeftContainer.add(createPreviewWidget(bundle), widgetMargin);	
		// Description
		colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false), widgetMargin);
			
		// ** RIGHT **
		// Programmatic Clients
		colRightContainer.add(createProgrammaticClientsWidget(bundle));
		// Provenance Widget for anything other than projects of folders
		if(!(bundle.getEntity() instanceof Project || bundle.getEntity() instanceof Folder)) 
			colRightContainer.add(createProvenanceWidget(bundle), widgetMargin);
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle, canEdit, readOnly, false), widgetMargin);
		
		// ** FULL WIDTH **
		// ***** TODO : BOTH OF THESE SHOULD BE REPLACED BY THE NEW ATTACHMENT/MARKDOWN SYSTEM ************
		// Child Browser
		if(DisplayUtils.hasChildrenOrPreview(bundle)){
			colLeftContainer.add(createEntityFilesBrowserWidget(bundle.getEntity(), true));
		}
		// ************************************************************************************************		
	}
	
	// Render the Page entity	
	private void renderPageEntity(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly, MarginData widgetMargin) {
		//TODO: this should be in another Place!  A WikiPage place, since WikiPage is not an Entity
		// ** LEFT **
//		colLeftContainer.add(createPreviewWidget(bundle), widgetMargin);	
			
		// ** RIGHT **
		// ** FULL WIDTH **
		// Entity Metadata
//		fullWidthContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit, readOnly), new MarginData(0, 0, 0, 0));
//		entityMetadata.setEntityBundle(bundle, readOnly);
//		fullWidthContainer.add(entityMetadata.asWidget(), widgetMargin);
//		// Description
//		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false), widgetMargin);
//
//		// Child Page Browser
//		fullWidthContainer.add(createEntityPagesBrowserWidget(bundle.getEntity(), canEdit, parentWikiId));
		// ************************************************************************************************		
	}
	
	// Render the Folder entity
	private void renderFolderEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly2,
			MarginData widgetMargin) {
		// ** LEFT **
		//Folder is not Locationable, so locationableTitleBar.asWidget is not visible
		//fullWidthContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit, readOnly), new MarginData(0, 0, 0, 0));
		entityMetadata.setEntityBundle(bundle, readOnly);
		fullWidthContainer.add(entityMetadata.asWidget(), new MarginData(0));
		
		// ** RIGHT **
		// none

		// ** FULL WIDTH **
		// Child Browser
		fullWidthContainer.add(createEntityFilesBrowserWidget(bundle.getEntity(), false));
		// Description
		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false), widgetMargin);		
		
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
			String entityTypeDisplay, boolean isAdmin, final boolean canEdit, boolean readOnly2,
			MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		fullWidthContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit, readOnly), new MarginData(0, 0, 0, 0));
		entityMetadata.setEntityBundle(bundle, readOnly);
		fullWidthContainer.add(entityMetadata.asWidget(), widgetMargin);

		// ** RIGHT **
		//none
	
		// ** FULL WIDTH **
		// Description
		fullWidthContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true), widgetMargin);
		// Child Page Browser
		if (DisplayUtils.isInTestWebsite(cookies)) {
			fullWidthContainer.add(wikiPageWidget);
			wikiPageWidget.configure(bundle.getEntity().getId(), WidgetConstants.WIKI_OWNER_ID_ENTITY, canEdit, null);
			
			presenter.loadRootWikiPage(new AsyncCallback<WikiPage>() {
				public void onSuccess(WikiPage result) {
					//add the page viewer, and subpage browser
					wikiContainer.add(getWikiPageWidget(result.getMarkdown()));
					if (result != null) {
						wikiContainer.add(createPagesBrowserWidget(pagesBrowser, bundle.getEntity().getId(), WidgetConstants.WIKI_OWNER_ID_ENTITY, canEdit, result.getId()));	
					}
				};
				@Override
				public void onFailure(Throwable caught) {
					wikiContainer.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + caught.getMessage() + "</div>")));
				}
			});
		}
			
		// Child File Browser
		fullWidthContainer.add(createEntityFilesBrowserWidget(bundle.getEntity(), true));

		LayoutContainer threeCol = new LayoutContainer();
		threeCol.addStyleName("span-24 notopmargin");
		// Annotation widget
		LayoutContainer annotContainer = createPropertyWidget(bundle);
		annotContainer.addStyleName("span-7 notopmargin");
		threeCol.add(annotContainer);		
		threeCol.add(createSpacer());
		// ***** TODO : BOTH OF THESE SHOULD BE REPLACED BY THE NEW ATTACHMENT/MARKDOWN SYSTEM ************		
		// Attachments
		Widget attachContainer = createAttachmentsWidget(bundle, canEdit, readOnly, false);
		attachContainer.addStyleName("span-7 notopmargin");
		threeCol.add(attachContainer);
		threeCol.add(createSpacer());
		// ************************************************************************************************
		fullWidthContainer.add(threeCol, widgetMargin);
	}


	private LayoutContainer createSpacer() {
		LayoutContainer onewide = new LayoutContainer();
		onewide.setStyleName("span-1 notopmargin");		
		return onewide;
	}

	// Render Snapshot Entity
	// TODO: This rendering should be phased out in favor of a regular wiki page
	private void renderSummaryEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit, boolean readOnly2,
			MarginData widgetMargin) {
		// ** LEFT **
		// Entity Metadata
		colLeftContainer.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit, readOnly), new MarginData(0, 0, 0, 0));
		entityMetadata.setEntityBundle(bundle, readOnly);
		colLeftContainer.add(entityMetadata.asWidget(), widgetMargin);
		// Description
		colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true), widgetMargin);

		// ** RIGHT **
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle, canEdit, readOnly, false), widgetMargin);

		// ** FULL WIDTH **
		// Snapshot entity
		snapshotWidget.setSnapshot((Summary)bundle.getEntity(), canEdit, readOnly);
		fullWidthContainer.add(snapshotWidget.asWidget());
	}

	private Widget createProvenanceWidget(EntityBundle bundle) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.addStyleName("span-7 notopmargin right last");
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h4>" + DisplayConstants.PROVENANCE + "</h4>")));

	    // Create the property body
	    // the headers for properties.
		ProvenanceWidget provenanceWidget = ginInjector.getProvenanceRenderer();		
		provenanceWidget.setHeight(PROVENANCE_HEIGHT_PX);
	    provenanceWidget.buildTree(bundle.getEntity(), 1, false);
	    LayoutContainer border = new LayoutContainer();
	    border.setBorders(true);	    
	    border.add(provenanceWidget.asWidget());
	    lc.add(border);
	    lc.layout();
	    return lc;
	}

	private Widget createProgrammaticClientsWidget(EntityBundle bundle) {		
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("span-7 notopmargin");
		lc.setAutoHeight(true);
		LayoutContainer pcc = ProgrammaticClientCode.createLoadWidget(bundle.getEntity().getId(), synapseJSNIUtils, sageImageBundle);
		pcc.addStyleName("right");
		lc.add(pcc);
		lc.layout();
	    return lc;
	}

	private Widget createEntityFilesBrowserWidget(Entity entity, boolean showTitle) {
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
	
	public static Widget createPagesBrowserWidget(PagesBrowser pagesBrowser, String ownerId, String contentType, boolean canEdit, String parentWikiId) {
		pagesBrowser.configure(ownerId, contentType, DisplayConstants.PAGES, canEdit, parentWikiId);
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("left");
		lc.setStyleAttribute("margin", "0px 0px 20px 0px");
		lc.add(pagesBrowser.asWidget());
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
	    if(description == null || "".equals(description)) {
	    	lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>")));
			lc.layout();
	    } else {
	    	//(in resolving the markdown, it escapes any user html)
	    	//now resolve the markdown
	    	lc.add(markdownWidget);
	    	markdownWidget.setMarkdown(description, bundle.getEntity().getId(), WidgetConstants.WIKI_OWNER_ID_ENTITY, false);
	    }
	    
   		return lc;
	}
	
	private LayoutContainer createPropertyWidget(EntityBundle bundle) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h4>" + DisplayConstants.ANNOTATIONS + "</h4>")));

	    // Create the property body
	    // the headers for properties.
	    propertyWidget.setEntityBundle(bundle);
	    lc.add(propertyWidget.asWidget());
	    lc.layout();
		return lc;
	}

	private Widget createAttachmentsWidget(final EntityBundle bundle, boolean canEdit, boolean readOnly, boolean showWhenEmpty) {
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
        final String actionUrl =  baseURl+ "?" + DisplayUtils.ENTITY_PARAM_KEY + "=" + bundle.getEntity().getId() ;

        if(canEdit && !readOnly) {
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
