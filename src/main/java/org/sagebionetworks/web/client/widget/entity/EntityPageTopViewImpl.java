package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
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
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityDeletedEvent;
import org.sagebionetworks.web.client.events.EntityDeletedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.FileTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public static final String TABLES_API_DOCS_URL = "http://rest.synapse.org/#org.sagebionetworks.repo.web.controller.TableController";
	public static final String TABLES_LEARN_MORE_URL = "#!Wiki:syn2305384/ENTITY/61139";

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}
	
	@UiField
	Anchor wikiLink;
	@UiField
	Anchor fileLink;
	@UiField
	Anchor tablesLink;
	@UiField
	Anchor adminLink;
	@UiField
	DivElement navtabContainer;
	@UiField
	LIElement wikiListItem;
	@UiField
	LIElement filesListItem;
	@UiField
	LIElement tablesListItem;
	@UiField
	LIElement adminListItem;
	
	@UiField
	Button tableLearnMoreButton;
	@UiField
	Button tableAPIDocsButton;
	
	@UiField
	Div projectTitleUI;
	@UiField
	Anchor projectHeaderAnchor;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	
	private ActionMenu actionMenu;
	private FileTitleBar fileTitleBar;
	private PortalGinInjector ginInjector;
	
	private Breadcrumb breadcrumb;
	
	//project level info
	@UiField
	SimplePanel projectMetadataContainer;
	@UiField
	SimplePanel projectDescriptionContainer;
	@UiField
	SimplePanel projectActionMenuContainer;

	
	@UiField
	Row wikiTabContainer;
	@UiField
	Row filesTabContainer;
	@UiField
	Row tablesTabContainer;
	@UiField
	Row adminTabContainer;
	@UiField
	SimplePanel evaluationListContainer;
	
	@UiField
	SimplePanel wikiPageContainer;
	
	//files
	@UiField
	SimplePanel fileHistoryContainer;
	@UiField
	SimplePanel fileDescriptionContainer;
	@UiField
	SimplePanel fileSnapshotsContainer;
	@UiField
	SimplePanel fileBrowserContainer;
	@UiField
	SimplePanel filesWikiPageContainer;
	@UiField
	SimplePanel filePreviewContainer;
	@UiField
	SimplePanel fileProvenanceContainer;
	@UiField
	SimplePanel fileProgrammaticClientsContainer;
	@UiField
	SimplePanel fileModifiedAndCreatedContainer;
	@UiField
	SimplePanel fileBreadcrumbContainer;
	@UiField
	SimplePanel fileTitlebarContainer;
	@UiField
	SimplePanel locationableTitlebarContainer;
	@UiField
	SimplePanel fileMetadataContainer;
	@UiField
	SimplePanel fileActionMenuContainer;
	
	//tables
	@UiField
	SimplePanel tableBreadcrumbContainer;
	@UiField
	SimplePanel tableMetadataContainer;
	@UiField
	SimplePanel tableActionMenuContainer;
	@UiField
	SimplePanel tableActionControllerContainer;
	@UiField
	SimplePanel tableWidgetContainer;
	@UiField
	SimplePanel tableModifiedAndCreatedContainer;
	@UiField
	SimplePanel tableListWidgetContainer;
	
	private SnapshotWidget snapshotWidget;
	private FileHistoryWidget fileHistoryWidget;
	private TableListWidget tableListWidget;
	private Long versionNumber;
	private SynapseJSNIUtils synapseJSNIUtils;
	private EntityMetadata entityMetadata;
	private FilesBrowser projectFilesBrowser;
	private FilesBrowser folderFilesBrowser;
	private MarkdownWidget markdownWidget;
	private WikiPageWidget wikiPageWidget;
	private PreviewWidget previewWidget;
	private CookieProvider cookies;
	private GlobalApplicationState globalApplicationState;
	private boolean isProject = false;
	private EntityArea currentArea;
	private AdministerEvaluationsList evaluationList;
	private static int WIDGET_HEIGHT_PX = 270;
	private String currentProjectAnchorTargetId;
	
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle,
			ActionMenu actionMenu,
			FileTitleBar fileTitleBar,
			Breadcrumb breadcrumb,
			SnapshotWidget snapshotWidget,
			EntityMetadata entityMetadata, 
			FileHistoryWidget fileHistoryWidget, 
			SynapseJSNIUtils synapseJSNIUtils,
			AdministerEvaluationsList evaluationList,
			PortalGinInjector ginInjector, 
			FilesBrowser projectFilesBrowser,
			FilesBrowser folderFilesBrowser,
			MarkdownWidget markdownWidget, 
			WikiPageWidget wikiPageWidget,
			TableListWidget tableListWidget,
			PreviewWidget previewWidget, CookieProvider cookies,
			GlobalApplicationState globalApplicationState) {
		this.sageImageBundle = sageImageBundle;
		this.actionMenu = actionMenu;
		this.breadcrumb = breadcrumb;
		this.snapshotWidget = snapshotWidget;
		this.entityMetadata = entityMetadata;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.evaluationList = evaluationList;
		this.fileTitleBar = fileTitleBar;
		this.ginInjector = ginInjector;
		this.folderFilesBrowser = folderFilesBrowser;
		this.projectFilesBrowser = projectFilesBrowser;
		this.previewWidget = previewWidget;
		this.fileHistoryWidget = fileHistoryWidget;
		this.markdownWidget = markdownWidget;	//note that this will be unnecessary after description contents are moved to wiki markdown
		this.wikiPageWidget = wikiPageWidget;
		this.tableListWidget = tableListWidget;
		this.cookies = cookies;
		this.globalApplicationState = globalApplicationState;
		initWidget(uiBinder.createAndBindUi(this));
		fileHistoryContainer.add(fileHistoryWidget.asWidget());
		evaluationListContainer.add(evaluationList.asWidget());
		fileSnapshotsContainer.add(snapshotWidget.asWidget());
		fileTitlebarContainer.add(fileTitleBar.asWidget());
		tableListWidgetContainer.add(tableListWidget);

		initProjectLayout();

		initClickHandlers();
	}
	
	private void initProjectLayout() {
		wikiLink.setText(DisplayConstants.WIKI);
		wikiLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.WIKI));
		fileLink.setText(DisplayConstants.FILES);		
		fileLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.FILES));
		tablesLink.setHTML(DisplayConstants.TABLES + DisplayConstants.BETA_BADGE_HTML);		
		tablesLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.TABLES));
		adminLink.setText(DisplayConstants.CHALLENGE_ADMIN);
		adminLink.addClickHandler(getTabClickHandler(Synapse.EntityArea.ADMIN));
	}
	
	private void initClickHandlers() {
		projectHeaderAnchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				globalApplicationState.getPlaceChanger().goTo(new Synapse(currentProjectAnchorTargetId, null, null, null));
			}
		});
		
		tableLearnMoreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(TABLES_LEARN_MORE_URL, "", "");
			}
		});

		tableAPIDocsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.newWindow(TABLES_API_DOCS_URL, "", "");
			}
		});
	}
	
	private ClickHandler getTabClickHandler(final Synapse.EntityArea targetTab) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Change tabs locally (in view) for projects as long as requested tab does not requre a place change
				if(isProject && !presenter.isPlaceChangeForArea(targetTab)) {
					setTabSelected(targetTab, true);					
				} else {	
					// return to cached location
					presenter.gotoProjectArea(targetTab, currentArea); 
				}
				currentArea = targetTab;
			}
		};
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, UserProfile userProfile,
			String entityTypeDisplay, Long versionNumber, Synapse.EntityArea area, String areaToken, EntityHeader projectHeader) {
		this.versionNumber = versionNumber;
		this.currentArea = area;
		DisplayUtils.hide(adminListItem);
		clearContent();
		
		hideTabContent();
		
		// project header
		showProjectLink(projectHeader);
	
		// Custom layouts for certain entities
		boolean isFolderLike = bundle.getEntity() instanceof Folder || bundle.getEntity() instanceof Study || bundle.getEntity() instanceof Analysis;
		isProject = bundle.getEntity() instanceof Project;		
		String wikiPageId = null;
		if (Synapse.EntityArea.WIKI == area)
			wikiPageId = areaToken;
		if (isProject) {
			renderProjectEntity(bundle, entityTypeDisplay, area, wikiPageId);
		} else if (isFolderLike) {
			//render Study like a Folder rather than a File (until all of the old types are migrated to the new world of Files and Folders)
			renderFolderEntity(bundle, entityTypeDisplay, wikiPageId, projectHeader);
			if (currentArea == null) currentArea = EntityArea.FILES;
		} else if (bundle.getEntity() instanceof Summary) {
		    renderSummaryEntity(bundle, entityTypeDisplay, versionNumber);
		    if (currentArea == null) currentArea = EntityArea.FILES;
		} else if(bundle.getEntity() instanceof TableEntity) {
			renderTableEntity(bundle, entityTypeDisplay, projectHeader, areaToken);
		} else {
			// default entity view
			renderFileEntity(bundle, entityTypeDisplay, versionNumber, wikiPageId, projectHeader);
			if (currentArea == null) currentArea = EntityArea.FILES;
		}
		synapseJSNIUtils.setPageTitle(bundle.getEntity().getName() + " - " + bundle.getEntity().getId());
		synapseJSNIUtils.setPageDescription(bundle.getEntity().getDescription());

	}

	private void hideTabContent(){
		wikiTabContainer.setVisible(false);
		filesTabContainer.setVisible(false);
		tablesTabContainer.setVisible(false);
		adminTabContainer.setVisible(false);
	}
	
	private void clearContent() {
		projectMetadataContainer.clear();
		projectDescriptionContainer.clear();
		projectActionMenuContainer.clear();
		
		fileBreadcrumbContainer.clear();
		fileTitlebarContainer.setVisible(false);
		locationableTitlebarContainer.setVisible(false);
		fileMetadataContainer.clear();
		fileActionMenuContainer.clear();
		fileDescriptionContainer.clear();
		fileBrowserContainer.clear();
		filesWikiPageContainer.clear();
		filePreviewContainer.clear();
		fileProvenanceContainer.clear();
		fileProgrammaticClientsContainer.clear();
		fileModifiedAndCreatedContainer.clear();
		wikiPageContainer.clear();
		fileHistoryContainer.setVisible(false);
		fileSnapshotsContainer.setVisible(false);
		
		tableBreadcrumbContainer.clear();
		tableMetadataContainer.clear();
		tableActionMenuContainer.clear();
		tableActionControllerContainer.clear();
		tableWidgetContainer.clear();
		tableModifiedAndCreatedContainer.clear();
		tableListWidgetContainer.setVisible(false);
	}
	
	private void showProjectLink(final EntityHeader projectHeader) {
		currentProjectAnchorTargetId = projectHeader.getId();
		projectTitleUI.setVisible(true);
		projectHeaderAnchor.setText(projectHeader.getName());
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
		projectFilesBrowser.setEntityUpdatedHandler(fileBrowserUpdateHandler);
		folderFilesBrowser.setEntityUpdatedHandler(fileBrowserUpdateHandler);
		entityMetadata.setEntityUpdatedHandler(handler);
		fileHistoryWidget.setEntityUpdatedHandler(handler);
		actionMenu.setEntityDeletedHandler(new EntityDeletedHandler() {			
			@Override
			public void onDeleteSuccess(EntityDeletedEvent event) {
				presenter.entityDeleted(event);
			}
		});
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
		fileTitleBar.clearState();
	}

	/*
	 * Private Methods
	 */
	// Render the File entity	
	private void renderFileEntity(EntityBundle bundle, String entityTypeDisplay, Long versionNumber, String wikiPageId, EntityHeader projectHeader) {
		// tab container
		setTabSelected(EntityArea.FILES, false); // select files tab for file
		
		// add breadcrumbs
		fileBreadcrumbContainer.add(breadcrumb.asWidget(bundle.getPath(), EntityArea.FILES));
		// File Title Bar
		if (bundle.getEntity() instanceof FileEntity) {
			fileTitleBar.configure(bundle);
			fileTitlebarContainer.setVisible(true);
		} 	
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		fileMetadataContainer.add(entityMetadata.asWidget());
		// ActionMenu
		fileActionMenuContainer.add(actionMenu.asWidget(bundle, versionNumber));
				
		// File History
		fileHistoryWidget.setEntityBundle(bundle, versionNumber);
		fileHistoryContainer.setVisible(true);
		// Description
		fileDescriptionContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false));
		// Wiki
		addWikiPageWidget(filesWikiPageContainer, bundle, wikiPageId, null);

		// Preview & Provenance Row
		boolean provFullWidth = true;
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {			
			filePreviewContainer.add(getFilePreview(bundle));
			provFullWidth = false;
		}
		if(!(bundle.getEntity() instanceof Project || bundle.getEntity() instanceof Folder)) { 
			// Provenance Widget (for anything other than projects of folders)
			fileProvenanceContainer.add(createProvenanceWidget(bundle, provFullWidth));
		}	
		// Programmatic Clients
		fileProgrammaticClientsContainer.add(createProgrammaticClientsWidget(bundle, versionNumber));
		// Created By/Modified By
		fileModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), false));
	}
	
	private Widget createModifiedAndCreatedWidget(Entity entity, boolean addTopMargin)  {
		FlowPanel attributionPanel = new FlowPanel();
		UserBadge createdByBadge = ginInjector.getUserBadgeWidget();
		createdByBadge.configure(entity.getCreatedBy());
		
		UserBadge modifiedByBadge = ginInjector.getUserBadgeWidget();
		modifiedByBadge.configure(entity.getModifiedBy());
		
		InlineHTML inlineHtml = new InlineHTML(DisplayConstants.CREATED_BY);
		attributionPanel.add(inlineHtml);
		Widget createdByBadgeWidget = createdByBadge.asWidget();
		createdByBadgeWidget.addStyleName("movedown-7");
		attributionPanel.add(createdByBadgeWidget);
		
		inlineHtml = new InlineHTML(" on " + DisplayUtils.converDataToPrettyString(entity.getCreatedOn()) + "<br>" + DisplayConstants.MODIFIED_BY);
		
		attributionPanel.add(inlineHtml);
		Widget modifiedByBadgeWidget = modifiedByBadge.asWidget();
		modifiedByBadgeWidget.addStyleName("movedown-7");
		attributionPanel.add(modifiedByBadgeWidget);
		inlineHtml = new InlineHTML(" on " + DisplayUtils.converDataToPrettyString(entity.getModifiedOn()));
		
		attributionPanel.add(inlineHtml);
		
		if(addTopMargin) attributionPanel.addStyleName("margin-top-15");
		return attributionPanel;
	}

	private Widget getFilePreview(EntityBundle bundle) {		
		previewWidget.configure(bundle);
		Widget preview = previewWidget.asWidget();
		preview.addStyleName("highlight-box");
		preview.getElement().setAttribute("highlight-box-title", DisplayConstants.PREVIEW);
		preview.setHeight(WIDGET_HEIGHT_PX + "px");
		SimplePanel wrapper = new SimplePanel(preview);
		wrapper.addStyleName("col-md-6");
		return wrapper;
	}
	
	// Render the Folder entity
	private void renderFolderEntity(EntityBundle bundle,
			String entityTypeDisplay, String wikiPageId, EntityHeader projectHeader) {		
		setTabSelected(EntityArea.FILES, false); // select files tab for folder
		fileBreadcrumbContainer.add(breadcrumb.asWidget(bundle.getPath(), EntityArea.FILES));
		// ActionMenu
		fileActionMenuContainer.add(actionMenu.asWidget(bundle, versionNumber));
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		fileMetadataContainer.add(entityMetadata.asWidget());
		
		// Description
		fileDescriptionContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, false));
		// Wiki		
		addWikiPageWidget(filesWikiPageContainer, bundle, wikiPageId,  null);
		// Child Browser
		fileBrowserContainer.add(configureFilesBrowser(folderFilesBrowser, bundle.getEntity(), bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser()));
		// Created By/Modified By
		fileModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), true));
	}

	// Render the Project entity
	private void renderProjectEntity(final EntityBundle bundle,
			String entityTypeDisplay,
			Synapse.EntityArea area, String wikiPageId) {		
		// tab container
		setTabSelected(area, false);

		projectTitleUI.setVisible(false);
		// Project header: Metadata & Description
		entityMetadata.setEntityBundle(bundle, versionNumber); 		
		projectMetadataContainer.add(entityMetadata.asWidget());
		projectDescriptionContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true));
		// ActionMenu
		projectActionMenuContainer.add(actionMenu.asWidget(bundle, versionNumber));

		// Wiki Tab: Wiki
		addWikiPageWidget(wikiPageContainer, bundle, wikiPageId, area);
		
		// File Tab: Files, Annotations & old
		fileBrowserContainer.add(configureFilesBrowser(projectFilesBrowser, bundle.getEntity(), bundle.getPermissions().getCanCertifiedUserAddChild(), bundle.getPermissions().getIsCertifiedUser()));
		// Created By/Modified By
		fileModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), true));

		// Tables Tab
		tableListWidget.configure(bundle);
		tableListWidgetContainer.setVisible(true);
		
		// Admin Tab: evaluations
		evaluationList.configure(bundle.getEntity().getId(), new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isVisible) {
				if (isVisible)
					DisplayUtils.show(adminListItem);
			}
		});
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
		if(targetTab == null) targetTab = Synapse.EntityArea.WIKI; // select tab, set default if needed
		
		hideTabContent();
		
		wikiListItem.removeClassName("active");
		filesListItem.removeClassName("active");
		tablesListItem.removeClassName("active");
		adminListItem.removeClassName("active");
		wikiLink.addStyleName("link");
		fileLink.addStyleName("link");
		tablesLink.addStyleName("link");
		adminLink.addStyleName("link");
		
		LIElement tab; 
		Anchor link;
		
		if (targetTab == Synapse.EntityArea.WIKI) {
			tab = wikiListItem;
			link = wikiLink;
			wikiTabContainer.setVisible(true);
		} else if (targetTab == Synapse.EntityArea.FILES) {
			tab = filesListItem;
			link = fileLink;
			filesTabContainer.setVisible(true);
		} else if(targetTab == Synapse.EntityArea.TABLES) {
			tab = tablesListItem;
			link = tablesLink;
			tablesTabContainer.setVisible(true);
		} else {
			tab = adminListItem;
			link = adminLink;
			adminTabContainer.setVisible(true);
		}
		
		link.removeStyleName("link");
		tab.addClassName("active");
	}
	
	private void addWikiPageWidget(SimplePanel container, EntityBundle bundle, String wikiPageId, final Synapse.EntityArea area) {
		wikiPageWidget.clear();
		if (DisplayUtils.isWikiSupportedType(bundle.getEntity())) {
			Widget wikiW = wikiPageWidget.asWidget();
			final SimplePanel wrapper = new SimplePanel(wikiW);
			wrapper.addStyleName("panel panel-default panel-body margin-bottom-0-imp");
			if(!isProject) wrapper.addStyleName("margin-top-15");
				container.add(wrapper);
			boolean canEdit = bundle.getPermissions().getCanCertifiedUserEdit();
			wikiPageWidget.configure(new WikiPageKey(bundle.getEntity().getId(), ObjectType.ENTITY.toString(), wikiPageId, versionNumber), canEdit, new WikiPageWidget.Callback() {
				@Override
				public void pageUpdated() {
					presenter.fireEntityUpdatedEvent();
				}
				@Override
				public void noWikiFound() {
					if(isProject) {
						//if wiki area not specified and no wiki found, show Files tab instead for projects 
						// Note: The fix for SWC-1785 was to set this check to area == null.  Prior to this change it was area != WIKI.
						if(area == null) {							
							setTabSelected(Synapse.EntityArea.FILES, false);
						}
					} else {
						// hide description area, and add description command in the Tools menu
						wrapper.setVisible(false);
						actionMenu.showAddDescriptionCommand(new Callback() {
							@Override
							public void invoke() {
								wikiPageWidget.createPage(DisplayConstants.DEFAULT_ROOT_WIKI_NAME, new Callback() {
									@Override
									public void invoke() {
										//if successful, then show the wiki page and remove the special command from the action menu
										wrapper.setVisible(true);
										actionMenu.hideAddDescriptionCommand();
									}
								});
								
							}
						});
					}
				}
			}, true);
		}
	}
	
	// Render Snapshot Entity
	// TODO: This rendering should be phased out in favor of a regular wiki page
	private void renderSummaryEntity(EntityBundle bundle,
			String entityTypeDisplay, Long versionNumber) {
		// tab container
		setTabSelected(EntityArea.FILES, false); // select files tab for summary
		
		// File tab: everything
		entityMetadata.setEntityBundle(bundle, versionNumber);		
		fileMetadataContainer.add(entityMetadata.asWidget());		
		//File History
		fileHistoryWidget.setEntityBundle(bundle, versionNumber);
		fileHistoryContainer.setVisible(true);
		// Description
		fileDescriptionContainer.add(createDescriptionWidget(bundle, entityTypeDisplay, true));

		// Snapshot entity
		boolean readOnly = versionNumber != null;
		snapshotWidget.setSnapshot((Summary)bundle.getEntity(), bundle.getPermissions().getCanCertifiedUserAddChild(), readOnly);		
		fileSnapshotsContainer.setVisible(true);		
		// Created By/Modified By
		fileModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), true));
	}

	private void renderTableEntity(EntityBundle bundle, String entityTypeDisplay, EntityHeader projectHeader, String areaToken) {
		// tab container
		setTabSelected(EntityArea.TABLES, false); 
		
		// add breadcrumbs
		tableBreadcrumbContainer.add(breadcrumb.asWidget(bundle.getPath(), EntityArea.TABLES));		
		// TODO: Add table name?
		// Entity Metadata
		entityMetadata.setEntityBundle(bundle, versionNumber);
		tableMetadataContainer.add(entityMetadata.asWidget());
		// ActionMenu
		ActionMenuWidget actionMenu = ginInjector.createActionMenuWidget();
		tableActionMenuContainer.add(actionMenu.asWidget());
				
		// Action controller
		EntityActionController controller = ginInjector.createEntityActionController();
		tableActionControllerContainer.add(controller.asWidget());
		controller.configure(actionMenu, bundle, new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		});

		// Table
		QueryChangeHandler qch = new QueryChangeHandler() {			
			@Override
			public void onQueryChange(Query newQuery) {
				presenter.setTableQuery(newQuery);				
			}

			@Override
			public Query getQueryString() {
				return presenter.getTableQuery();
			}

			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		};
		IsWidget tableWidget = null;
		// V2
		TableEntityWidget v2TableWidget = ginInjector.createNewTableEntityWidget();
		v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), qch, actionMenu);
		tableWidget = v2TableWidget;
		Widget tableW = tableWidget.asWidget();
		tableWidgetContainer.add(tableW);
		// Created By/Modified By
		tableModifiedAndCreatedContainer.add(createModifiedAndCreatedWidget(bundle.getEntity(), false));
	}

	
	private Widget createProvenanceWidget(EntityBundle bundle, boolean fullWidth) {
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
	    provenanceWidget.configure(null, configMap, null, null);
	    final Widget provViewWidget = provenanceWidget.asWidget(); 
	    FlowPanel lc = new FlowPanel();
	    lc.addStyleName("highlight-box");
	    lc.getElement().setAttribute(WebConstants.HIGHLIGHT_BOX_TITLE, DisplayConstants.PROVENANCE);
	    lc.add(provViewWidget);
	    SimplePanel wrapper = new SimplePanel(lc);
	    String width = fullWidth ? "col-md-12" : "col-md-6";
	    wrapper.addStyleName(width);
		return wrapper;
	}
	
	private Widget createProgrammaticClientsWidget(EntityBundle bundle, Long versionNumber) {
		return ProgrammaticClientCode.createLoadWidget(bundle.getEntity().getId(), versionNumber, synapseJSNIUtils, sageImageBundle);
	}

	private Widget configureFilesBrowser(FilesBrowser filesBrowser, Entity entity, boolean canCertifiedUserAddChild, boolean isCertifiedUser) {
		filesBrowser.configure(entity.getId(), canCertifiedUserAddChild, isCertifiedUser);
		return filesBrowser.asWidget();
	}
	
	private Widget createDescriptionWidget(final EntityBundle bundle, String entityTypeDisplay, boolean showWhenEmpty) {
		final FlowPanel lc = new FlowPanel();
		String description = bundle.getEntity().getDescription();
	
		if(!showWhenEmpty) {
			if(description == null || "".equals(description))
				return lc;
		}
		
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"clear: left;\"></div>")));
	
		// Add the description body
	    if(description != null && !("".equals(description))) {
    		Label plainDescriptionText = new Label();
    		plainDescriptionText.addStyleName("wiki-description");
    		plainDescriptionText.setText(description);
    		lc.add(plainDescriptionText);
	    }
	    
   		return lc;
	}
	
}
