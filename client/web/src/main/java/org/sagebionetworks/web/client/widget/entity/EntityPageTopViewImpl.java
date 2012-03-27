package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.adminmenu.AdminMenu;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.editpanels.AnnotationEditor;
import org.sagebionetworks.web.client.widget.editpanels.NodeEditor;
import org.sagebionetworks.web.client.widget.entity.children.EntityChildBrowser;
import org.sagebionetworks.web.client.widget.entity.menu.ActionMenu;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowFactory;
import org.sagebionetworks.web.client.widget.licenseddownloader.LicensedDownloader;
import org.sagebionetworks.web.client.widget.portlet.SynapsePortlet;
import org.sagebionetworks.web.client.widget.sharing.AccessMenuButton;
import org.sagebionetworks.web.client.widget.table.QueryTableFactory;
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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.cell.client.widget.PreviewDisclosurePanel;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}
	
	@UiField
	SimplePanel breadcrumbsPanel;
	@UiField
	SimplePanel actionMenuPanel;
	@UiField
	SimplePanel titlePanel;
	@UiField
	SimplePanel metadataPanel;
	@UiField
	SimplePanel descriptionHeader;
	@UiField
	SimplePanel propertiesHeader;
	@UiField
	SimplePanel descriptionBody;
	@UiField
	SimplePanel propertiesBody;
	@UiField
	SimplePanel contentsTitle;
	@UiField
	SimplePanel contentBody;
	@UiField 
	SimplePanel portalPanelThreeCol;
	@UiField
	SimplePanel portalPanelRstudio;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private PreviewDisclosurePanel previewDisclosurePanel;	
	private AnnotationEditor annotationEditor;
	private AdminMenu adminMenu;
	private ActionMenu actionMenu;
	private EntityChildBrowser entityChildBrowser;
	private LicensedDownloader licensedDownloader;
	private QueryTableFactory queryTableFactory;
	private Breadcrumb breadcrumb;
	private boolean isAdministrator = false; 
	private boolean canEdit = false;
	PropertyWidget propertyWidget;
			
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton, NodeEditor nodeEditor,
			PreviewDisclosurePanel previewDisclosurePanel,
			AnnotationEditor annotationEditor, AdminMenu adminMenu, ActionMenu actionMenu,
			EntityChildBrowser entityChildBrowser, LicensedDownloader licensedDownloader, Breadcrumb breadcrumb, 
			QueryTableFactory queryTableFactory, PropertyWidget propertyWidget) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.previewDisclosurePanel = previewDisclosurePanel;
		this.annotationEditor = annotationEditor;
		this.adminMenu = adminMenu;
		this.actionMenu = actionMenu;
		this.entityChildBrowser = entityChildBrowser;
		this.licensedDownloader = licensedDownloader;
		this.breadcrumb = breadcrumb;
		this.queryTableFactory = queryTableFactory;
		this.propertyWidget = propertyWidget;
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void setEntityBundle(EntityBundle bundle, String entityTypeDisplay, boolean isAdministrator, boolean canEdit) {
		
		
		// check authorization
		this.isAdministrator = isAdministrator;
		this.canEdit = canEdit;
		// add breadcrumbs
		breadcrumbsPanel.clear();
		breadcrumbsPanel.add(breadcrumb.asWidget(bundle.getPath()));

		//setup action menu
		actionMenuPanel.clear();		
		actionMenu.addEntityUpdatedHandler(new EntityUpdatedHandler() {			
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				presenter.fireEntityUpdatedEvent();
			}
		});
		actionMenuPanel.add(actionMenu.asWidget(bundle, isAdministrator, canEdit));
		
		// Portal #1 - 2 columns
		Portal portalTwoCol = new Portal(2);  
	    portalTwoCol.setBorders(false);  
	    portalTwoCol.setStyleAttribute("backgroundColor", "white");  
	    portalTwoCol.setColumnWidth(0, .57);  
	    portalTwoCol.setColumnWidth(1, .43);	 	    
	    Html title = new Html();
	    
	    // Title
	    titlePanel.clear();
	    titlePanel.add(createTitleHtml(bundle.getEntity(), entityTypeDisplay));
	    // Metadata
	    metadataPanel.clear();
	    metadataPanel.add(createMetadata(bundle.getEntity()));

	    // the headers for description and property
	    descriptionHeader.clear();
	    descriptionHeader.add(new Html("<h4>Description</h4>"));

	    // the headers for properties.
	    propertiesHeader.clear();
	    propertiesHeader.add(new Html("<h4>Properties &amp; Annotations</h4>"));
	    // Add the description body
	    descriptionBody.clear();
	    descriptionBody.add(new Html(bundle.getEntity().getDescription()));
	    // Create the property body
	    propertyWidget.setEntityBundle(bundle);
	    propertiesBody.clear();
	    propertiesBody.add(propertyWidget.asWidget());
	    // the cotnents panel
	    contentsTitle.clear();
	    contentsTitle.add(new Html("<h4>"+entityTypeDisplay+" Contents</h4>"));
	    contentBody.clear();
	    contentBody.add(createEntityChildBrowser(bundle.getEntity(), canEdit));
	    
	    
		// Portal #2 - full width
		Portal portalSingleCol = new Portal(1);  
	    portalSingleCol.setBorders(false);  
	    portalSingleCol.setStyleAttribute("backgroundColor", "white");  
	    portalSingleCol.setColumnWidth(0, 1.0);  	 	    
	    
		// Portal #3 - 3 columns
		Portal portalThreeCol = new Portal(3);  
		portalThreeCol.setBorders(false);  
		portalThreeCol.setStyleAttribute("backgroundColor", "white");  
		portalThreeCol.setColumnWidth(0, .33);  	 	    
		portalThreeCol.setColumnWidth(1, .33);  	 	    
		portalThreeCol.setColumnWidth(2, .33);  	 	    
		portalPanelThreeCol.clear();
		portalPanelThreeCol.add(portalThreeCol);
	    // Create R Client portlet
	    portalThreeCol.add(createRClientPortlet(bundle.getEntity()), 0);
	    // Create References portlet
	    portalThreeCol.add(createReferencesPortlet(bundle.getEntity(), bundle.getReferencedBy()), 1);
	    // Create References portlet
	    portalThreeCol.add(createActivityFeedPortlet(bundle.getEntity()), 2);
	    
	    Portal portalRstudio = new Portal(1);
	    portalRstudio.setBorders(false);
	    portalRstudio.setStyleAttribute("backgroundColor", "white");  
	    portalRstudio.setColumnWidth(0, 1.0);
	    portalPanelRstudio.clear();
	    portalPanelRstudio.add(portalRstudio);
	    portalRstudio.add(createRstudioPortlet(bundle.getEntity()), 0);
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
		// TODO : add other widgets here
	}
	
	/*
	 * Private Methods
	 */
	private SynapsePortlet createDescriptionPortlet(Entity entity) {		
		SynapsePortlet portlet = new SynapsePortlet("Description");
		String overviewText = entity.getDescription();
		if(overviewText == null) overviewText = "";
		int summaryLength = overviewText.length() >= DisplayConstants.DESCRIPTION_SUMMARY_LENGTH ? DisplayConstants.DESCRIPTION_SUMMARY_LENGTH : overviewText.length();
		previewDisclosurePanel.init("Expand", overviewText.substring(0, summaryLength), overviewText);
		portlet.add(previewDisclosurePanel);
		
		// download button		
		if(presenter.isLocationable()) {
			portlet.add(licensedDownloader.asWidget(entity, false), new MarginData(10, 0, 0, 0));
		}
		
		// add some properties		
		String propString = "Modified By " + entity.getModifiedBy() + " on "
		+ DisplayUtils.convertDateToString(entity.getModifiedOn())
		+ "<br/>" 
		+ "Created By " + entity.getCreatedBy()
		+ " on "
		+ DisplayUtils.convertDateToString(entity.getCreatedOn());
		
		if(entity instanceof Versionable) {
			Versionable e = (Versionable)entity;
			propString += "<br/>" + "Version " + e.getVersionLabel();
		}
		
		Html propHtml = new Html(propString);
		propHtml.setStyleName(DisplayUtils.STYLE_SMALL_GREY_TEXT);		
		portlet.add(propHtml, new MarginData(10,0,0,0));
		
		return portlet;
	}
	
	

	private Html createTitleHtml(Entity entity, String entityTypeDisplay) {
		StringBuilder builder = new StringBuilder();
		builder.append("<h3>");
		builder.append("<span style=\"font-weight:lighter;\">[");
		builder.append(entityTypeDisplay.substring(0, 1));
		builder.append("]</span> ");
		builder.append(entity.getName());
		builder.append(" (");
		builder.append(entity.getId());
		builder.append(")");
		builder.append("</h3>");
	    return new Html(builder.toString());
	}
	
	/**
	 * Basic meata data about this entity
	 * @param entity
	 * @return
	 */
	private Html createMetadata(Entity entity) {
		StringBuilder builder = new StringBuilder();
		builder.append("<div style=\"font-size: 80%\">");
		builder.append("Added by: ");
		builder.append(entity.getCreatedBy());
		builder.append(" on: ");
		builder.append(entity.getCreatedOn());
		builder.append("<br/>Last updated by: ");
		builder.append(entity.getModifiedBy());
		builder.append(" on: ");
		builder.append(entity.getModifiedOn());
		builder.append("<br/>");
		if(entity instanceof Versionable){
			Versionable vb = (Versionable) entity;
			builder.append("Current version: ");
			builder.append(vb.getVersionLabel());
			builder.append(" (#");
			builder.append(vb.getVersionNumber());
			builder.append(")");
		}
		builder.append("<div/>");
	    return new Html(builder.toString());
	}

	private Portlet createAnnotationEditorPortlet(Entity entity) {
	    SynapsePortlet portlet = new SynapsePortlet("Properties &amp; Annotations", true, false);  
	    portlet.setLayout(new FitLayout());    
	    portlet.setAutoHeight(true);  	  
		annotationEditor.setPlaceChanger(presenter.getPlaceChanger());
		annotationEditor.setResource(DisplayUtils.getNodeTypeForEntity(entity), entity.getId());				
		portlet.add(annotationEditor.asWidget());
		return portlet;
	}	
	
	
	
	private Portlet createRClientPortlet(Entity entity) {			  
	    SynapsePortlet portlet = new SynapsePortlet("Synapse R Client");  
	    portlet.setLayout(new FitLayout());    
	    portlet.setAutoHeight(true);  	  

	    // setup install code widgets		
	    Html loadEntityCode = new Html(DisplayUtils.getRClientEntityLoad(entity.getId()));
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
		
		portlet.add(new Html("<p>The Synapse R Client allows you to interact with the Synapse system programmatically.</p>"));
		portlet.add(loadEntityCode);
		portlet.add(vp);
	    return portlet;  
	}	
	
	private Portlet createReferencesPortlet(Entity entity, PaginatedResults<EntityHeader> referencedBy) {			  
	    SynapsePortlet portlet = new SynapsePortlet("Others Using this " + DisplayUtils.getEntityTypeDisplay(entity));	    
	    portlet.setLayout(new FitLayout());    
	    portlet.setAutoHeight(true);  	  
	    
	    if(referencedBy.getTotalNumberOfResults() > 0) {	    
		    List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		    			
		    // ref by column
			ColumnConfig colConfig = new ColumnConfig("id", "Referenced By", 200);			
			columns.add(colConfig);		
			GridCellRenderer<BaseModelData> cellRenderer = configureReferencedByGridCellRenderer();
			colConfig.setRenderer(cellRenderer);
			colConfig.setSortable(false);
			
			// type column
			ColumnConfig colConfigType = new ColumnConfig("type", "Type", 80);
			colConfigType.setSortable(false);
			columns.add(colConfigType);						
			
		    ColumnModel cm = new ColumnModel(columns);
		    
		    // TODO : eventually remove this block
		    if(DisplayConstants.showDemoHtml && DisplayConstants.MSKCC_DATASET_DEMO_ID.equals(entity.getId())) {					
				portlet.add(new HTML(DisplayConstants.DEMO_ANALYSIS));
				return portlet;
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
								model.set("id", header.getId());
								model.set("name", header.getName());
								model.set("type", header.getType());
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
	        loader.setLimit(10);
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
			
			portlet.add(cp);
			
			// load initial data
			loader.load();

	    } else {
	    	portlet.add(new Html(DisplayConstants.TEXT_NO_REFERENCES + " " + DisplayUtils.getEntityTypeDisplay(entity) + "."));
	    }
	    return portlet;  
	}	
	
	private GridCellRenderer<BaseModelData> configureReferencedByGridCellRenderer() {
		// configure cell renderer
		GridCellRenderer<BaseModelData> cellRenderer = new GridCellRenderer<BaseModelData>() {
			public String render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				// catch all for types that don't need specific rendering beyond string
				if("id".equals(property) && model.get("id") != null) {
					return presenter.createEntityLink(model.get("id").toString(), null, model.get("name").toString());
				} else {
					return null;
				}
			}
		};
		return cellRenderer;
	}			

	
	private Portlet createActivityFeedPortlet(Entity entity) {			  
	    SynapsePortlet portlet = new SynapsePortlet("Activity Feed");  
	    portlet.setLayout(new FitLayout());    
	    portlet.setAutoHeight(true);
	    
		if(DisplayConstants.showDemoHtml && DisplayConstants.MSKCC_DATASET_DEMO_ID.equals(entity.getId())) {			
			portlet.add(new HTML(DisplayConstants.DEMO_COMMENTS));
		}

	    return portlet;  
	}	
	
	private Widget createEntityChildBrowser(Entity entity, boolean canEdit) {
		return entityChildBrowser.asWidget(entity, canEdit);
	}
	
	private Portlet createRstudioPortlet(Entity entity) {
	    final SynapsePortlet portlet = new SynapsePortlet("RStudio");  
	    portlet.setLayout(new FitLayout());    
	    portlet.setAutoHeight(true);	    	    
	    
	    final SplitButton showRstudio = new SplitButton("&nbsp;&nbsp;Load in RStudio Server");
	    showRstudio.setIcon(AbstractImagePrototype.create(iconsImageBundle.rstudio24()));
	    Menu menu = new Menu();  
	    MenuItem item = new MenuItem("Set RStudio Server URL");
		item.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				final MessageBox box = MessageBox.prompt("Set RStudio Server URL", "RStudio Server URL:");
				box.getTextBox().setValue(presenter.getRstudioUrlBase());
				box.addCallback(new Listener<MessageBoxEvent>() {
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getText().equals("Cancel")) // .isCanceled() does not give correct result
							return;
						if (be.getValue() != null && !be.getValue().equals("")) {
							presenter.saveRStudioUrlBase(be.getValue());
							showRstudio.fireEvent(Events.Select);
						}
					}
				});
			}
		});
	    menu.add(item);  
	    showRstudio.setMenu(menu);  
	  
	    showRstudio.setHeight(36);
	    if(!presenter.isLoggedIn()) {
	    	showRstudio.disable();
	    	showRstudio.setText("&nbsp;&nbsp;Please Login to Load in RStudio");
	    }
	    
	    final Html label = new Html("<div class=\"span-12 notopmargin\">" +
	    		"<a href=\"http://rstudio.org/\" class=\"link\">RStudio&trade;</a> is a free and open source integrated development environment (IDE) for R. " +
	    		"You can run it on your desktop (Windows, Mac, or Linux) or even over the web using RStudio Server.<br/></br>" +
	    		"If you do not have a copy of RStudio Server setup, you can create one by <a href=\"http://rstudio.org/download/server\" class=\"link\">following these directions</a>." +
	    "</div>");
	    	    
	    showRstudio.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
			    Frame iframe = new Frame(presenter.getRstudioUrl());
			    iframe.setHeight("500px");
			    iframe.setWidth("100%");
			    portlet.remove(showRstudio);
			    portlet.remove(label);
			    portlet.add(iframe);
				portlet.layout(true);
			}
		});	    
	    portlet.add(showRstudio);
	    portlet.add(label, new MarginData(5, 0, 0, 0));
		
	    return portlet;  		
	}
	
}
