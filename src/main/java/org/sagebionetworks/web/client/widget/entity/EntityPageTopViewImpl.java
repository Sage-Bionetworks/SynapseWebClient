package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.Analysis;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Study;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
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
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.LocationableTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
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
	SimplePanel topFullWidthPanel;

	@UiField
	Anchor wikiLink;
	@UiField
	Anchor fileLink;
	@UiField
	Anchor adminLink;
	@UiField
	DivElement navtabContainer;
	@UiField
	LIElement wikiListItem;
	@UiField
	LIElement filesListItem;
	@UiField
	LIElement adminListItem;
	@UiField
	SimplePanel projectTitleContainer;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private ActionMenu actionMenu;
	private LocationableTitleBar locationableTitleBar;
	private FileTitleBar fileTitleBar;
	private PortalGinInjector ginInjector;
	private EntityTreeBrowser entityTreeBrowser;
	private Breadcrumb breadcrumb;
	private AnnotationsWidget annotationsWidget;
	private LayoutContainer fullWidthContainer;
	private LayoutContainer topFullWidthContainer, currentTabContainer, wikiTabContainer, filesTabContainer, adminTabContainer;
	private Attachments attachmentsPanel;
	private SnapshotWidget snapshotWidget;
	private FileHistoryWidget fileHistoryWidget;
	private Long versionNumber;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityMetadata entityMetadata;
	private FilesBrowser filesBrowser;
	private MarkdownWidget markdownWidget;
	private WikiPageWidget wikiPageWidget;
	private PreviewWidget previewWidget;
	private CookieProvider cookies;
	GlobalApplicationState globalApplicationState;
	private boolean isProject = false;
	
	private static int WIDGET_HEIGHT_PX = 270;
	
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton,
			ActionMenu actionMenu,
			LocationableTitleBar locationableTitleBar,
			FileTitleBar fileTitleBar,
			EntityTreeBrowser entityTreeBrowser, Breadcrumb breadcrumb,
			AnnotationsWidget propertyWidget,
			Attachments attachmentsPanel, SnapshotWidget snapshotWidget,
			EntityMetadata entityMetadata, FileHistoryWidget fileHistoryWidget, SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector, 
			FilesBrowser filesBrowser, 
			MarkdownWidget markdownWidget, 
			WikiPageWidget wikiPageWidget, 
			PreviewWidget previewWidget, CookieProvider cookies,
			GlobalApplicationState globalApplicationState) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.actionMenu = actionMenu;
		this.entityTreeBrowser = entityTreeBrowser;
		this.breadcrumb = breadcrumb;
		this.annotationsWidget = propertyWidget;
		this.attachmentsPanel = attachmentsPanel;
		this.snapshotWidget = snapshotWidget;
		this.entityMetadata = entityMetadata;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.locationableTitleBar = locationableTitleBar;
		this.fileTitleBar = fileTitleBar;
		this.ginInjector = ginInjector;
		this.filesBrowser = filesBrowser;
		this.previewWidget = previewWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.markdownWidget = markdownWidget;	//note that this will be unnecessary after description contents are moved to wiki markdown
		this.wikiPageWidget = wikiPageWidget;
		this.cookies = cookies;
		this.globalApplicationState = globalApplicationState;
		initWidget(uiBinder.createAndBindUi(this));
		initProjectLayout();
	}
	
	private void initProjectLayout() {
		currentTabContainer = new LayoutContainer();
		currentTabContainer.addStyleName("tab-background margin-left-neg-15 margin-right-neg-15");
		wikiTabContainer = new LayoutContainer();
		wikiTabContainer.addStyleName("margin-left-15 margin-right-15 padding-top-15");
		filesTabContainer = new LayoutContainer();
		filesTabContainer.addStyleName("margin-left-15 margin-right-15 fileTabTopPadding");
		adminTabContainer = new LayoutContainer();
		adminTabContainer.addStyleName("margin-left-15 margin-right-15");
		wikiLink.setText(DisplayConstants.PROJECT_WIKI);
		wikiLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.WIKI));
		fileLink.setText(DisplayConstants.FILES);
		fileLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.FILES));
		adminLink.setText(DisplayConstants.CHALLENGE_ADMIN);
		adminLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.ADMIN));
	}
	
	private ClickHandler getTabClickHandler(final Synapse.EntityArea targetTab) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Change tabs for projects, change places for other entity types
				if(isProject) {
					setTabSelected(targetTab, true);					
				} else {					
					presenter.gotoProjectArea(targetTab); // change place back to the project
				}
			}
		};
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile,
			String entityTypeDisplay, boolean isAdministrator, boolean canEdit,
			Long versionNumber, Synapse.EntityArea area, String areaToken, EntityHeader projectHeader) {
		this.versionNumber = versionNumber;		
		fullWidthContainer = initContainerAndPanel(fullWidthContainer, fullWidthPanel);
		topFullWidthContainer = initContainerAndPanel(topFullWidthContainer, topFullWidthPanel);

		fullWidthContainer.removeAll();
		topFullWidthContainer.removeAll();
		adminListItem.addClassName("hide");
		currentTabContainer.removeAll();
		wikiTabContainer.removeAll();
		filesTabContainer.removeAll();
		adminTabContainer.removeAll();
				
		// project header
		fillProjectLink(projectHeader);
	
		// Custom layouts for certain entities
		boolean isFolderLike = bundle.getEntity() instanceof Folder || bundle.getEntity() instanceof Study || bundle.getEntity() instanceof Analysis;
		isProject = bundle.getEntity() instanceof Project;
		String wikiPageId = null;
		if (Synapse.EntityArea.WIKI == area)
			wikiPageId = areaToken;
		if (isProject) {
			renderProjectEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, area, wikiPageId);
		} else if (isFolderLike) {
			//render Study like a Folder rather than a File (until all of the old types are migrated to the new world of Files and Folders)
			renderFolderEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, wikiPageId, projectHeader);
		} else if (bundle.getEntity() instanceof Summary) {
		    renderSummaryEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, versionNumber);
		} else {
			// default entity view
			renderFileEntity(bundle, entityTypeDisplay, isAdministrator, canEdit, versionNumber, wikiPageId, projectHeader);
		}
		synapseJSNIUtils.setPageTitle(bundle.getEntity().getName() + " - " + bundle.getEntity().getId());
		synapseJSNIUtils.setPageDescription(bundle.getEntity().getDescription());

		fullWidthContainer.layout(true);
		topFullWidthContainer.layout(true);
	}

	private void fillProjectLink(final EntityHeader projectHeader) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant(AbstractImagePrototype.create(iconsImageBundle.synapseProject24()).getHTML())
		.appendHtmlConstant("<span class=\"dropLargeIconText\"> ")
		.appendEscaped(projectHeader.getName())
		.appendHtmlConstant("</span>");
		Anchor a = new Anchor(shb.toSafeHtml());
		a.addStyleName("projectTitle");
		a.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Synapse(projectHeader.getId(), null, null, null));
			}
		});
		projectTitleContainer.setWidget(a);
		projectTitleContainer.setVisible(true);
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
		EntityUpdatedHandler fileBrowserUpdateHandler = new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
//				if (isProject)
//					presenter.refreshTab(Synapse.EntityTab.FILES, null);
//				else
					presenter.fireEntityUpdatedEvent();
			}
		};
		
		filesBrowser.setEntityUpdatedHandler(fileBrowserUpdateHandler);
		entityMetadata.setEntityUpdatedHandler(handler);
		annotationsWidget.setEntityUpdatedHandler(handler);
		fileHistoryWidget.setEntityUpdatedHandler(handler);
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
		if (fullWidthContainer != null)
			fullWidthContainer.removeAll();
	}


	/*
	 * Private Methods
	 */
	// Render the File entity	
	private void renderFileEntity(EntityBundle bundle, String entityTypeDisplay, boolean isAdmin, boolean canEdit, Long versionNumber, String wikiPageId, EntityHeader projectHeader) {
		// tab container
		fullWidthContainer.add(currentTabContainer);		
		setTabSelected(EntityArea.FILES, false); // select files tab for file
		
		// ** LEFT/RIGHT
		LayoutContainer row;
		row = DisplayUtils.createRowContainer();
		LayoutContainer left = new LayoutContainer();
		left.addStyleName("col-md-8");
		LayoutContainer right = new LayoutContainer();
		right.addStyleName("col-md-4");
		row.add(left);
		row.add(right);
		filesTabContainer.add(row);		
		// add breadcrumbs
		left.add(breadcrumb.asWidget(bundle.getPath()));
		// File Title Bar
		if (bundle.getEntity() instanceof FileEntity) {
			left.add(fileTitleBar.asWidget(bundle, isAdmin, canEdit), new MarginData(0, 0, 0, 0));
		} else {
			left.add(locationableTitleBar.asWidget(bundle, isAdmin, canEdit), new MarginData(0, 0, 0, 0));
		}		
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		left.add(entityMetadata.asWidget());
		// ActionMenu
		right.add(actionMenu.asWidget(bundle, isAdmin, canEdit, versionNumber));
				
		// File History
		fileHistoryWidget.setEntityBundle(bundle, versionNumber);
		filesTabContainer.add(fileHistoryWidget.asWidget());
		// Description
		filesTabContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false));
		
		// Preview & Provenance Row
		row = DisplayUtils.createRowContainer();
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {			
			row.add(getFilePreview(bundle));
		}
		if(!(bundle.getEntity() instanceof Project || bundle.getEntity() instanceof Folder)) { 
			// Provenance Widget (for anything other than projects of folders)
			row.add(createProvenanceWidget(bundle));
		}
		filesTabContainer.add(row);
		
		// Annotations			
		filesTabContainer.add(createAnnotationsWidget(bundle, canEdit));		
		// Attachments
		filesTabContainer.add(createAttachmentsWidget(bundle, canEdit, false));		
		// Wiki
		addWikiPageWidget(filesTabContainer, bundle, canEdit, wikiPageId, 24, true);

		// LEFT/RIGHT Bottom
		row = DisplayUtils.createRowContainer();
		left = new LayoutContainer();
		left.addStyleName("col-md-8");
		right = new LayoutContainer();
		right.addStyleName("col-md-4 push-right");
		row.add(left);
		row.add(right);
		filesTabContainer.add(row);				
		// Programmatic Clients
		right.add(createProgrammaticClientsWidget(bundle, versionNumber));

	}
	
	private Widget getFilePreview(EntityBundle bundle) {		
		previewWidget.configure(bundle);
		Widget preview = previewWidget.asWidget();
		preview.addStyleName("highlight-box");
		preview.setTitle(DisplayConstants.PREVIEW);
		preview.setHeight(WIDGET_HEIGHT_PX + "px");
		SimplePanel wrapper = new SimplePanel(preview);
		wrapper.addStyleName("col-md-6");
		return wrapper;
	}
	
	// Render the Folder entity
	private void renderFolderEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit, String wikiPageId, EntityHeader projectHeader) {		
		// tab container
		fullWidthContainer.add(currentTabContainer);
		setTabSelected(EntityArea.FILES, false); // select files tab for folder
		
		// ** LEFT/RIGHT
		LayoutContainer row;
		row = DisplayUtils.createRowContainer();
		LayoutContainer left = new LayoutContainer();
		left.addStyleName("col-md-8");
		LayoutContainer right = new LayoutContainer();
		right.addStyleName("col-md-4");
		row.add(left);
		row.add(right);
		filesTabContainer.add(row);		
		// add breadcrumbs
		left.add(breadcrumb.asWidget(bundle.getPath()));
		// ActionMenu
		right.add(actionMenu.asWidget(bundle, isAdmin, canEdit, versionNumber));


		// File tab: everything
		entityMetadata.setEntityBundle(bundle, versionNumber);
		Widget ebW = entityMetadata.asWidget();
		row = DisplayUtils.createRowContainer();
		ebW.addStyleName("col-md-12");
		row.add(ebW);
		filesTabContainer.add(row);		
		// Description
		filesTabContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false));

		// Child Browser
		row = DisplayUtils.createRowContainer();
		row.add(createEntityFilesBrowserWidget(bundle.getEntity(), false, canEdit));
		filesTabContainer.add(row);
		
		//Annotations
		filesTabContainer.add(createAnnotationsWidget(bundle, canEdit));
		filesTabContainer.layout(true);
		
		// Wiki
		addWikiPageWidget(filesTabContainer, bundle, canEdit, wikiPageId, 24, true);
			
	}

	// Render the Project entity
	private void renderProjectEntity(final EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, final boolean canEdit,
			Synapse.EntityArea area, String wikiPageId) {		
		// tab container
		fullWidthContainer.add(currentTabContainer);				
		if(area == null) area = Synapse.EntityArea.WIKI; // select tab, set default if needed
		setTabSelected(area, false);

		projectTitleContainer.setVisible(false);
		
		// ** LEFT/RIGHT
		LayoutContainer row;
		row = DisplayUtils.createRowContainer();
		LayoutContainer left = new LayoutContainer();
		left.addStyleName("col-md-8");
		LayoutContainer right = new LayoutContainer();
		right.addStyleName("col-md-4");
		row.add(left);
		row.add(right);
		topFullWidthContainer.add(row, new MarginData(5, 0, 0, 0));		

		// Project header: Metadata & Description
		entityMetadata.setEntityBundle(bundle, versionNumber); 		
		left.add(entityMetadata.asWidget());
		left.add(createDescriptionWidget(bundle, entityTypeDisplay, true));
		// ActionMenu
		right.add(actionMenu.asWidget(bundle, isAdmin, canEdit, versionNumber));

		// Wiki Tab: Wiki
		addWikiPageWidget(wikiTabContainer, bundle, canEdit, wikiPageId, 24, false);
		
		// File Tab: Files, Annotations & old
		row = DisplayUtils.createRowContainer();		
		row.add(createEntityFilesBrowserWidget(bundle.getEntity(), false, canEdit));
		filesTabContainer.add(row);			
		filesTabContainer.add(createAnnotationsWidget(bundle, canEdit));		
		filesTabContainer.add(createAttachmentsWidget(bundle, canEdit, false)); // Attachments (TODO : this should eventually be removed)
		
		// Admin Tab: evaluations
		row = DisplayUtils.createRowContainer();
		row.add(createEvaluationAdminList(bundle, new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isVisible) {
				if (isVisible)
					adminListItem.removeClassName("hide");
			}
		}));
		adminTabContainer.add(row);
	}

	/**
	 * Used only for setting the view's tab display
	 * @param targetTab
	 * @param userSelected 
	 */
	private void setTabSelected(Synapse.EntityArea targetTab, boolean userSelected) {
		// tell presenter what tab we're on only if the user clicked
		// this keeps extra goTos that break navigation from occurring 
		if(userSelected) presenter.setArea(targetTab, null);
		
		wikiListItem.removeClassName("active");
		filesListItem.removeClassName("active");
		adminListItem.removeClassName("active");
		wikiLink.addStyleName("link");
		fileLink.addStyleName("link");
		adminLink.addStyleName("link");
		
		LIElement tab; 
		Anchor link;
		LayoutContainer targetContainer;
		
		if (targetTab == Synapse.EntityArea.WIKI) {
			tab = wikiListItem;
			link = wikiLink;
			targetContainer = wikiTabContainer;
		} else if (targetTab == Synapse.EntityArea.FILES) {
			tab = filesListItem;
			link = fileLink;
			targetContainer = filesTabContainer;
		} else {
			tab = adminListItem;
			link = adminLink;
			targetContainer = adminTabContainer;
		}
		
		link.removeStyleName("link");
		tab.addClassName("active");
		
		currentTabContainer.removeAll();
		currentTabContainer.add(targetContainer);
		currentTabContainer.layout(true);							
	}
	
	private void addWikiPageWidget(LayoutContainer container, EntityBundle bundle, boolean canEdit, String wikiPageId, int spanWidth, boolean marginTop) {
		wikiPageWidget.clear();
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
			// Child Page Browser
			Widget wikiW = wikiPageWidget.asWidget();
			SimplePanel wrapper = new SimplePanel(wikiW);
			wrapper.addStyleName("panel panel-default panel-body");
			if(marginTop) wrapper.addStyleName("margin-top-15");
			container.add(wrapper);
			wikiPageWidget.configure(new WikiPageKey(bundle.getEntity().getId(), ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					presenter.fireEntityUpdatedEvent();
				}
				@Override
				public void noWikiFound() {
					//no wiki found, show Files tab instead for projects
					if(isProject) setTabSelected(Synapse.EntityArea.FILES, false);					
				}
			}, true, spanWidth);
		}
	}
	
	// Render Snapshot Entity
	// TODO: This rendering should be phased out in favor of a regular wiki page
	private void renderSummaryEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean isAdmin, boolean canEdit, Long versionNumber) {
		// tab container
		fullWidthContainer.add(currentTabContainer);
		setTabSelected(EntityArea.FILES, false); // select files tab for summary
		
		// File tab: everything
		LayoutContainer row;
		entityMetadata.setEntityBundle(bundle, versionNumber);
		Widget ebW = entityMetadata.asWidget();
		row = DisplayUtils.createRowContainer();
		ebW.addStyleName("col-md-12");
		row.add(ebW);
		filesTabContainer.add(row);		
		//File History
		filesTabContainer.add(fileHistoryWidget.asWidget(), new MarginData(0));
		// Description
		filesTabContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true));

		// Snapshot entity
		boolean readOnly = versionNumber != null;
		snapshotWidget.setSnapshot((Summary)bundle.getEntity(), canEdit, readOnly);
		Widget ssW = snapshotWidget.asWidget();
		ssW.addStyleName("panel panel-body margin-top-15");
		filesTabContainer.add(ssW);
		
		//Annotations
		filesTabContainer.add(createAnnotationsWidget(bundle, canEdit));
		filesTabContainer.layout(true);
		// Attachments
		filesTabContainer.add(createAttachmentsWidget(bundle, canEdit, false));
	}

	private Widget createProvenanceWidget(EntityBundle bundle) {
		final LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.addStyleName("highlight-box");
		lc.setTitle(DisplayConstants.PROVENANCE);
		
	    // Create the property body
	    // the headers for properties.
		ProvenanceWidget provenanceWidget = ginInjector.getProvenanceRenderer();						
		
		Map<String,String> configMap = new HashMap<String,String>();
		Long version = bundle.getEntity() instanceof Versionable ? ((Versionable)bundle.getEntity()).getVersionNumber() : null; 
		configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(bundle.getEntity().getId(), version));
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(1));		
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(WIDGET_HEIGHT_PX-84));
	    provenanceWidget.configure(null, configMap);
	    final Widget provViewWidget = provenanceWidget.asWidget(); 
	    final LayoutContainer border = new LayoutContainer();
	    border.add(provViewWidget);
		
	    lc.add(border);
	    lc.layout();
	    SimplePanel wrapper = new SimplePanel(lc);
	    wrapper.addStyleName("col-md-6");
		return wrapper;
	}
	
	private Widget createEvaluationAdminList(EntityBundle bundle, CallbackP<Boolean> isChallengeCallback) {
		// Create the property body
	    // the headers for properties.
		AdministerEvaluationsList list = ginInjector.getAdministerEvaluationsList();						
		list.configure(bundle.getEntity().getId(), isChallengeCallback);
		 
		return list.asWidget();
	}

	private Widget createProgrammaticClientsWidget(EntityBundle bundle, Long versionNumber) {		
		LayoutContainer lc = new LayoutContainer();
		lc.addStyleName("panel");
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
		lc.addStyleName("col-md-12 margin-top-10");
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
		    		markdownWidget.setMarkdown(description, new WikiPageKey(bundle.getEntity().getId(),  ObjectType.ENTITY.toString(), null, versionNumber), false, false);
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
	
	private Widget createAnnotationsWidget(EntityBundle bundle, boolean canEdit) {
	    // Create the property body
	    // the headers for properties.
	    annotationsWidget.configure(bundle, canEdit);
	    Widget widget;
		if (canEdit || !annotationsWidget.isEmpty()) {
			widget = annotationsWidget.asWidget();
			widget.addStyleName("highlight-box");
			widget.setTitle(DisplayConstants.ANNOTATIONS);
		} else {
			widget = new HTML();
		}
		return widget;
	}

	private Widget createAttachmentsWidget(final EntityBundle bundle, boolean canEdit, boolean showWhenEmpty) {	    
		LayoutContainer lc = new LayoutContainer();
	    lc.setTitle(DisplayConstants.BUTTON_WIKI_ATTACHMENTS);	    		
		lc.setStyleName("highlight-box"); 
        final String baseURl = GWT.getModuleBaseURL()+"attachment";

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
