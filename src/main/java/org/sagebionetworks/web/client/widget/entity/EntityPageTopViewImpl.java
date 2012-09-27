package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Summary;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_REQUIRED;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.children.EntityChildBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAttachmentDialog;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.cell.client.widget.PreviewDisclosurePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	private static final String REFERENCES_KEY_ID = "id";
	private static final String REFERENCES_KEY_NAME = "name";
	private static final String REFERENCES_KEY_TYPE = "type";

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
	private PreviewDisclosurePanel previewDisclosurePanel;
	private ActionMenu actionMenu;
	private EntityChildBrowser entityChildBrowser;
	private Breadcrumb breadcrumb;
	private PropertyWidget propertyWidget;
	private LayoutContainer colLeftContainer;
	private LayoutContainer colRightContainer;
	private LayoutContainer fullWidthContainer;
	private EntityTypeProvider entityTypeProvider;
	private Attachments attachmentsPanel;
	private SnapshotWidget snapshotWidget;
	private boolean readOnly = false;
	private boolean rStudioUrlReady = false;
	private SplitButton showRstudio;
	private SynapseJSNIUtils synapseJSNIUtils;
	private TitleWidget titleWidget;

	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton,
			PreviewDisclosurePanel previewDisclosurePanel,
			ActionMenu actionMenu,
			EntityChildBrowser entityChildBrowser, Breadcrumb breadcrumb,
			PropertyWidget propertyWidget,EntityTypeProvider entityTypeProvider,
			Attachments attachmentsPanel, SnapshotWidget snapshotWidget,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.previewDisclosurePanel = previewDisclosurePanel;
		this.actionMenu = actionMenu;
		this.entityChildBrowser = entityChildBrowser;
		this.breadcrumb = breadcrumb;
		this.propertyWidget = propertyWidget;
		this.entityTypeProvider = entityTypeProvider;
		this.attachmentsPanel = attachmentsPanel;
		this.snapshotWidget = snapshotWidget;
		this.synapseJSNIUtils = synapseJSNIUtils;

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
		actionMenu.addEntityUpdatedHandler(new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		});
		actionMenuPanel.add(actionMenu.asWidget(bundle, isAdministrator,
				canEdit, readOnly));

		MarginData widgetMargin = new MarginData(0, 0, 20, 0);

		// Custom layouts for certain entities
		if(bundle.getEntity() instanceof Summary) {
		    renderSnapshotEntity(bundle, userProfile, entityTypeDisplay, canEdit, readOnly, widgetMargin);
		} else if (bundle.getEntity() instanceof Folder) {
			renderFolderEntity(bundle, entityTypeDisplay, canEdit, readOnly, widgetMargin);
		} else if (bundle.getEntity() instanceof Project) {
			renderProjectEntity(bundle, entityTypeDisplay, canEdit, readOnly, widgetMargin);
		} else {
			// default entity view
			renderDefaultEntity(bundle, entityTypeDisplay, canEdit, readOnly, widgetMargin);
		}

		colLeftContainer.layout(true);
		colRightContainer.layout(true);
		fullWidthContainer.layout(true);
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
		// TODO
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		actionMenu.clearState();
		if (colLeftContainer != null)
			colLeftContainer.removeAll();
		if (colRightContainer != null)
			colRightContainer.removeAll();
		if (fullWidthContainer != null)
			fullWidthContainer.removeAll();
		if (titleWidget != null) {
			titleWidget.clear();
			titleWidget = null;
		}
	}

	@Override
	public void setRStudioUrlReady() {
		// allow button to be used now
		if(showRstudio != null) {
			showRstudio.enable();
		}
	}

	/*
	 * Private Methods
	 */
	private void renderFolderEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean canEdit, boolean readOnly2,
			MarginData widgetMargin) {
		// TODO: make this actually render folders differently
		renderDefaultEntity(bundle, entityTypeDisplay, canEdit, readOnly, widgetMargin);
	}

	private void renderProjectEntity(EntityBundle bundle,
			String entityTypeDisplay, boolean canEdit, boolean readOnly2,
			MarginData widgetMargin) {
		// TODO: make this actually render projects differently
		renderDefaultEntity(bundle, entityTypeDisplay, canEdit, readOnly, widgetMargin);
	}

	private void renderDefaultEntity(EntityBundle bundle, String entityTypeDisplay, boolean canEdit, boolean readOnly, MarginData widgetMargin) {
		// ** LEFT **
		// Title
		titleWidget = new TitleWidget(bundle, createRestrictionWidget(), entityTypeDisplay, iconsImageBundle, canEdit, readOnly, synapseJSNIUtils);
		colLeftContainer.add(titleWidget.asWidget(), widgetMargin);
		// Description
		colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay), widgetMargin);
		// Child Browser
		if(DisplayUtils.hasChildrenOrPreview(bundle)){
			colLeftContainer.add(createEntityChildBrowserWidget(bundle.getEntity()), widgetMargin);
		}
		// Attachment preview is only added when there are previews.
		if(DisplayUtils.hasAttachmentPreviews(bundle.getEntity())){
			colLeftContainer.add(createAttachmentPreview(bundle.getEntity()), widgetMargin);
		}
		// RStudio Button
//		colLeftContainer.add(createRstudioWidget(bundle.getEntity()), widgetMargin);
		// Create Activity Feed widget
//		colLeftContainer.add(createActivityFeedWidget(bundle.getEntity()), widgetMargin);

		// ** LEFT **
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle, canEdit, readOnly), widgetMargin);
		// Create References widget
		colRightContainer.add(createReferencesWidget(bundle.getEntity(), bundle.getReferencedBy()), widgetMargin);
		// Create R Client widget
		colRightContainer.add(createRClientWidget(bundle.getEntity()), widgetMargin);

		// ** FULL WIDTH **
		// none.
	}
	
	private Widget createRestrictionWidget() {
		if (!presenter.includeRestrictionWidget()) return null;
		boolean isAnonymous = presenter.isAnonymous();
		boolean hasAdministrativeAccess = false;
		boolean hasFulfilledAccessRequirements = false;
		String jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = presenter.hasAdministrativeAccess();
			jiraFlagLink = presenter.getJiraFlagUrl();
		}
		APPROVAL_REQUIRED restrictionLevel = presenter.getRestrictionLevel();
		String accessRequirementText = null;
		Callback touAcceptanceCallback = null;
		Callback requestACTCallback = null;
		Callback imposeRestrictionsCallback = presenter.getImposeRestrictionsCallback();
		Callback loginCallback = presenter.getLoginCallback();
		if (restrictionLevel!=APPROVAL_REQUIRED.NONE) {
			accessRequirementText = presenter.accessRequirementText();
			if (restrictionLevel==APPROVAL_REQUIRED.LICENSE_ACCEPTANCE) {
				touAcceptanceCallback = presenter.accessRequirementCallback();
			} else {
				// get the Jira link for ACT approval
				if (!isAnonymous) {
					requestACTCallback = new Callback() {
						@Override
						public void invoke() {
							Window.open(presenter.getJiraRequestAccessUrl(), "_blank", "");
							
						}
					};
				}
			}
			if (!isAnonymous) hasFulfilledAccessRequirements = presenter.hasFulfilledAccessRequirements();
		}
		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink, 
				isAnonymous, 
				hasAdministrativeAccess,
				accessRequirementText,
				touAcceptanceCallback,
				requestACTCallback,
				imposeRestrictionsCallback,
				loginCallback,
				restrictionLevel, 
				hasFulfilledAccessRequirements,
				iconsImageBundle,
				synapseJSNIUtils);
	}


	private void renderSnapshotEntity(EntityBundle bundle, UserProfile userProfile,
			String entityTypeDisplay, boolean canEdit, boolean readOnly, MarginData widgetMargin) {

		titleWidget = new TitleWidget(bundle, createRestrictionWidget(), entityTypeDisplay, iconsImageBundle, canEdit, readOnly, synapseJSNIUtils);
		colLeftContainer.add(titleWidget.asWidget(), widgetMargin);
		// Description
		colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay), widgetMargin);

		// ** RIGHT **
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle, canEdit, readOnly), widgetMargin);

		// ** FULL WIDTH **
		// Snapshot entity
		snapshotWidget.setSnapshot((Summary)bundle.getEntity(), canEdit, readOnly);
		fullWidthContainer.add(snapshotWidget.asWidget());

	}

	private Widget createRClientWidget(Entity entity) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new Html(SafeHtmlUtils.fromSafeConstant("<h3>Synapse R Client</h3>").asString()));

	    // setup install code widgets
		SafeHtml rClientLoad = DisplayUtils.getRClientEntityLoad(entity.getId());
	    Html loadEntityCode = new Html(rClientLoad.asString());
	    loadEntityCode.setStyleName(DisplayUtils.STYLE_CODE_CONTENT);

		final LayoutContainer container = new LayoutContainer();
	    String rSnipet = "# " + DisplayConstants.LABEL_R_CLIENT_INSTALL
		+ "<br/>" + DisplayUtils.R_CLIENT_DOWNLOAD_CODE;
		container.addText(rSnipet);
	    container.setStyleName(DisplayUtils.STYLE_CODE_CONTENT);
	    container.setVisible(false);


		Button getRClientButton = new Button(DisplayConstants.BUTTON_SHOW_R_CLIENT_INSTALL,
				new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						if (container.isVisible()) {
							container.el().slideOut(Direction.UP, FxConfig.NONE);
						} else {
							container.setVisible(true);
							container.el().slideIn(Direction.DOWN, FxConfig.NONE);
						}
					}

				});
		getRClientButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.cog16()));

		VerticalPanel vp = new VerticalPanel();
		vp.add(getRClientButton);
		vp.add(container);

		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<p>The Synapse R Client allows you to interact with the Synapse system programmatically.</p>")));
		lc.add(loadEntityCode);
		lc.add(vp);

		lc.layout();
	    return lc;
	}

	private Widget createReferencesWidget(Entity entity, PaginatedResults<EntityHeader> referencedBy) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h3>Others Using this ").appendEscaped(entityTypeProvider.getEntityDispalyName(entity)).appendHtmlConstant("</h3>");
		lc.add(new HTML(shb.toSafeHtml()));


	    if(referencedBy.getTotalNumberOfResults() > 0) {
		    List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

		    // ref by column
			ColumnConfig colConfig = new ColumnConfig(REFERENCES_KEY_ID, "Referenced By", 200);
			columns.add(colConfig);
			GridCellRenderer<BaseModelData> cellRenderer = configureReferencedByGridCellRenderer();
			colConfig.setRenderer(cellRenderer);
			colConfig.setSortable(false);

		    ColumnModel cm = new ColumnModel(columns);

		    // TODO : eventually remove this block
		    if(DisplayConstants.showDemoHtml && DisplayConstants.MSKCC_DATASET_DEMO_ID.equals(entity.getId())) {
				lc.add(new HTML(DisplayConstants.DEMO_ANALYSIS));
				return lc;
			}

		    // CREATE TABLE
		    // add table of references
	        RpcProxy<PagingLoadResult<BaseModelData>> proxy = new RpcProxy<PagingLoadResult<BaseModelData>>() {
	            @Override
	            public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<BaseModelData>> callback) {
	            	int offset = ((PagingLoadConfig) loadConfig).getOffset();
	            	int limit =  ((PagingLoadConfig) loadConfig).getLimit();
	            	presenter.loadShortcuts(offset, limit, new AsyncCallback<PaginatedResults<EntityHeader>>() {
						@Override
						public void onSuccess(PaginatedResults<EntityHeader> result) {
							List<BaseModelData> dataList = new ArrayList<BaseModelData>();
							for(EntityHeader header : result.getResults()) {
								BaseModelData model = new BaseModelData();
								model.set(REFERENCES_KEY_ID, header.getId());
								model.set(REFERENCES_KEY_NAME, header.getName());
								model.set(REFERENCES_KEY_TYPE, header.getType());

								dataList.add(model);
							}
							PagingLoadResult<BaseModelData> loadResultData = new BasePagingLoadResult<BaseModelData>(dataList);
							loadResultData.setTotalLength((int) result.getTotalNumberOfResults());
							//loadResultData.setOffset(result.get);

							callback.onSuccess(loadResultData);
						}
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});
	            }
	        };

	        // create a paging loader from the proxy
	        BasePagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
	        loader.setRemoteSort(false);
	        loader.setReuseLoadConfig(true);
	        loader.setLimit(200);
	        loader.setOffset(0);

	        // add initial data to the store
			ListStore<BaseModelData> store = new ListStore<BaseModelData>(loader);

			Grid<BaseModelData> grid = new Grid<BaseModelData>(store, cm);
			grid.setLayoutData(new FitLayout());
			grid.setStateful(false);
			grid.setLoadMask(true);
			grid.getView().setForceFit(true);
			grid.setAutoWidth(false);
			grid.setStyleAttribute("borderTop", "none");
			grid.setBorders(false);
			grid.setStripeRows(true);
			grid.setHeight(200);

			ContentPanel cp = new ContentPanel();
			cp.setLayout(new FitLayout());
			cp.setBodyBorder(true);
			cp.setButtonAlign(HorizontalAlignment.CENTER);
			cp.setHeaderVisible(false);
			cp.setHeight(200);

			// create bottom paging toolbar
		    PagingToolBar toolBar = new PagingToolBar(10);
	        toolBar.bind(loader);
	        toolBar.setSpacing(2);
	        toolBar.insert(new SeparatorToolItem(), toolBar.getItemCount() - 2);

	    	cp.setBottomComponent(toolBar);

			cp.add(grid);

			lc.add(cp);

			// load initial data
			loader.load();

	    } else {
	    	SafeHtmlBuilder shbNoRef = new SafeHtmlBuilder();
	    	shbNoRef.appendHtmlConstant(DisplayConstants.TEXT_NO_REFERENCES + " ").appendEscaped(entityTypeProvider.getEntityDispalyName(entity)).appendHtmlConstant(".");
	    	lc.add(new HTML(shbNoRef.toSafeHtml()));
	    }
	    lc.layout();
	    return lc;
	}

	private GridCellRenderer<BaseModelData> configureReferencedByGridCellRenderer() {
		// configure cell renderer
		GridCellRenderer<BaseModelData> cellRenderer = new GridCellRenderer<BaseModelData>() {
			public String render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				// catch all for types that don't need specific rendering beyond string
				if(REFERENCES_KEY_ID.equals(property) && model.get(REFERENCES_KEY_ID) != null) {
					return DisplayUtils.getIconHtml(presenter.getIconForType(model.get(REFERENCES_KEY_TYPE).toString()))
							+ " "
							+ presenter.createEntityLink(model.get(REFERENCES_KEY_ID).toString(),null, model.get(REFERENCES_KEY_NAME).toString());
				} else {
					return null;
				}
			}
		};
		return cellRenderer;
	}


	private Widget createActivityFeedWidget(Entity entity) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>Activity Feed</h3>")));

		if(DisplayConstants.showDemoHtml && DisplayConstants.MSKCC_DATASET_DEMO_ID.equals(entity.getId())) {
			lc.add(new HTML(SafeHtmlUtils.fromSafeConstant(DisplayConstants.DEMO_COMMENTS)));
		} else {
			lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_ACTIVITY + "</div>")));
		}

		lc.layout();
	    return lc;
	}

	private Widget createEntityChildBrowserWidget(Entity entity) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		String typeDisplay = entityTypeProvider.getEntityDispalyName(entity);
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h3>").appendEscaped(typeDisplay).appendHtmlConstant("Contents</h3>");
		lc.add(new HTML(shb.toSafeHtml()));
		lc.add(entityChildBrowser.asWidget(entity));
		lc.layout();
		return lc;
	}


	private Widget createDescriptionWidget(EntityBundle bundle, String entityTypeDisplay) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>Description</h3>")));


		// Add the description body
	    String description = bundle.getEntity().getDescription();
	    SafeHtml descriptionSafeHtml = null;
	    if(description == null || "".equals(description)) {
	    	descriptionSafeHtml = SafeHtmlUtils.fromSafeConstant("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>");
	    } else {
	    	// escape user content, but convert new lines to <br>
	    	SafeHtmlBuilder shb = new SafeHtmlBuilder();
    		shb.appendEscapedLines(description);
    		descriptionSafeHtml = shb.toSafeHtml();
	    }
	    lc.add(new HTML(descriptionSafeHtml));

		lc.layout();
		return lc;
	}

	private Widget createPropertyWidget(EntityBundle bundle) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>Properties &amp; Annotations</h3>")));

	    // Create the property body
	    // the headers for properties.
	    propertyWidget.setEntityBundle(bundle);
	    lc.add(propertyWidget.asWidget());
	    lc.layout();
		return lc;
	}

	/**
	 * Create the attachment preview.
	 * @param entity
	 * @return
	 */
	private Widget createAttachmentPreview(Entity entity) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new HTML(SafeHtmlUtils.fromSafeConstant("<h3>Visual Attachments</h3>")));
		String baseURl = GWT.getModuleBaseURL()+"attachment";
		SafeHtmlBuilder bodyBuilder = new SafeHtmlBuilder();
		int col = 0;
		List<AttachmentData> list = entity.getAttachments();
		bodyBuilder.appendHtmlConstant("<div class=\"span-17 left notopmargin\">");
		for(AttachmentData data: list){
			// Ignore all attachemtns without a preview.
			if(data.getPreviewId() == null) continue;
			// Add a new span
			String style = "span-5 left";
			if(col == 2 || col == list.size()-1 ){
				style = style+" last";
				col = 0;
			}else{
				col++;
			}
			bodyBuilder.appendHtmlConstant("<div class=\"" + style + "\">");
			bodyBuilder.appendHtmlConstant("<div class=\"preview-image-loading view\" >");
			// Using a table to center the image because the alternatives were not an option for this case.
			// The solution is from: http://stackoverflow.com/questions/388180/how-to-make-an-image-center-vertically-horizontally-inside-a-bigger-div
			// We could not use the background approach because we are using a background sprit to show loading.
			// We could not use the second approach because we do not know the dimensions of the image (we just know its width<=160 and height<=160).
			// That left the third option...a table that causes people to go blind!
			bodyBuilder.appendHtmlConstant(DisplayUtils.IMAGE_CENTERING_TABLE_START)
			.appendHtmlConstant("<a class=\"item-preview spec-border-ie\" href=\""
					+ DisplayUtils.createAttachmentUrl(baseURl, entity.getId(), data.getTokenId(), data.getName())
					+ "\" target=\"_blank\" name=\""
					+ SafeHtmlUtils.fromString(data.getName()).asString()
					+ "\">")
			.appendHtmlConstant("<img style=\"margin:auto; display:block;\" src=\""
					+ DisplayUtils.createAttachmentUrl(baseURl, entity.getId(), data.getPreviewId(), null)
					+ "\" />")
			.appendHtmlConstant("</a>")
			.appendHtmlConstant(DisplayUtils.IMAGE_CENTERING_TABLE_END)
			.appendHtmlConstant("</div>")
			.appendHtmlConstant("</div>");
		}
		bodyBuilder.appendHtmlConstant("</div>");
		lc.add(new HTML(bodyBuilder.toSafeHtml()));
		lc.layout();
		return lc;
	}

	private Widget createAttachmentsWidget(EntityBundle bundle, boolean canEdit, boolean readOnly) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
        LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
        c.setLayout(layout);

        c.add(new Html("<h3>Attachments</h3>"), new HBoxLayoutData(new Margins(0, 5, 0, 0)));
        HBoxLayoutData flex = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        flex.setFlex(1);

        String baseURl = GWT.getModuleBaseURL()+"attachment";
        final String actionUrl =  baseURl+ "?" + DisplayUtils.ENTITY_PARAM_KEY + "=" + bundle.getEntity().getId();

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
        attachmentsPanel.configure(baseURl, bundle.getEntity());
        lc.add(attachmentsPanel.asWidget());
		lc.layout();
		return lc;
	}


	@Override
	public void setEntityVersions(Versionable entity, TreeMap<Long, String> versions) {
		if (titleWidget != null) {
			titleWidget.setVersions(entity, versions);
		}
	}

}
