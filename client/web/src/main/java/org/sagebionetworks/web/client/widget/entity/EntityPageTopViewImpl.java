package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.children.EntityChildBrowser;
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
	SimplePanel colLeftPanel;
	@UiField
	SimplePanel colRightPanel;
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
	private EntityTypeProvider entityTypeProvider;
			
	@Inject
	public EntityPageTopViewImpl(Binder uiBinder,
			SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle,
			AccessMenuButton accessMenuButton,
			PreviewDisclosurePanel previewDisclosurePanel,
			ActionMenu actionMenu,
			EntityChildBrowser entityChildBrowser, Breadcrumb breadcrumb, 
			PropertyWidget propertyWidget,EntityTypeProvider entityTypeProvider) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		this.previewDisclosurePanel = previewDisclosurePanel;
		this.actionMenu = actionMenu;
		this.entityChildBrowser = entityChildBrowser;
		this.breadcrumb = breadcrumb;
		this.propertyWidget = propertyWidget;
		this.entityTypeProvider = entityTypeProvider;
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	
	@Override
	public void setEntityBundle(EntityBundle bundle, String entityTypeDisplay, boolean isAdministrator, boolean canEdit) {
		
		if(colLeftContainer == null) {
			colLeftContainer = new LayoutContainer();
			colLeftContainer.setAutoHeight(true);
			colLeftContainer.setAutoWidth(true);
			colLeftPanel.clear();
			colLeftPanel.add(colLeftContainer);
		}
		if(colRightContainer == null) {
			colRightContainer = new LayoutContainer();
			colRightContainer.setAutoHeight(true);
			colRightContainer.setAutoWidth(true);
			colRightPanel.clear();
			colRightPanel.add(colRightContainer);
		}
		
		colLeftContainer.removeAll();
		colRightContainer.removeAll();
		
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
		
		MarginData widgetMargin = new MarginData(0, 0, 20, 0);
		
		/* 
		 * Left Column
		 */
	    // Title
	    colLeftContainer.add(createTitleWidget(bundle, entityTypeDisplay), widgetMargin);
	    // Description
	    colLeftContainer.add(createDescriptionWidget(bundle, entityTypeDisplay), widgetMargin);	    
	    // Child Browser
		colLeftContainer.add(createEntityChildBrowserWidget(bundle.getEntity(), canEdit), widgetMargin);
	    // RStudio Button
		colLeftContainer.add(createRstudioWidget(bundle.getEntity()), widgetMargin);
		// Create Activity Feed widget
		colLeftContainer.add(createActivityFeedWidget(bundle.getEntity()), widgetMargin);
	    
		/*
		 * Right Column
		 */
		// Annotation Editor widget
		colRightContainer.add(createPropertyWidget(bundle), widgetMargin);
		// Attachments
		colRightContainer.add(createAttachmentsWidget(bundle), widgetMargin);
	    // Create References widget
		colRightContainer.add(createReferencesWidget(bundle.getEntity(), bundle.getReferencedBy()), widgetMargin);
		// Create R Client widget
		colRightContainer.add(createRClientWidget(bundle.getEntity()), widgetMargin);
	    
		colLeftContainer.layout(true);
		colRightContainer.layout(true);
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
	
	private Widget createRClientWidget(Entity entity) {			  
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new Html("<h3>Synapse R Client</h3>"));  

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
		
		lc.add(new Html("<p>The Synapse R Client allows you to interact with the Synapse system programmatically.</p>"));
		lc.add(loadEntityCode);
		lc.add(vp);
		
		lc.layout();
	    return lc;  
	}	
	
	private Widget createReferencesWidget(Entity entity, PaginatedResults<EntityHeader> referencedBy) {			  
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		lc.add(new Html("<h3>Others Using this " + entityTypeProvider.getEntityDispalyName(entity) + "/<h3>"));	    

	    
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
			
			lc.add(cp);
			
			// load initial data
			loader.load();

	    } else {
	    	lc.add(new Html(DisplayConstants.TEXT_NO_REFERENCES + " " + entityTypeProvider.getEntityDispalyName(entity) + "."));
	    }
	    lc.layout();
	    return lc;  
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

	
	private Widget createActivityFeedWidget(Entity entity) {			  
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		lc.add(new Html("<h3>Activity Feed</h3>"));  
	    
		if(DisplayConstants.showDemoHtml && DisplayConstants.MSKCC_DATASET_DEMO_ID.equals(entity.getId())) {			
			lc.add(new HTML(DisplayConstants.DEMO_COMMENTS));
		} else {
			lc.add(new Html("<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_ACTIVITY + "</div>"));
		}
				
		lc.layout();
	    return lc;  
	}	
	
	private Widget createEntityChildBrowserWidget(Entity entity, boolean canEdit) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		String typeDisplay = entityTypeProvider.getEntityDispalyName(entity);
		lc.add(new Html("<h3>" + typeDisplay + " " + "Contents</h3>"));
		lc.add(entityChildBrowser.asWidget(entity, canEdit));
		lc.layout();
		return lc;
	}
	
	
	private Widget createRstudioWidget(Entity entity) {
		final LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		lc.add(new Html("<h3>RStudio</h3>"));  	        	    	    	 
	    
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
	    
	    final Html label = new Html("<a href=\"http://rstudio.org/\" class=\"link\">RStudio&trade;</a> is a free and open source integrated development environment (IDE) for R. " +
	    		"You can run it on your desktop (Windows, Mac, or Linux) or even over the web using RStudio Server.<br/></br>" +
	    		"If you do not have a copy of RStudio Server setup, you can create one by <a href=\"http://rstudio.org/download/server\" class=\"link\">following these directions</a>.");
	    	    
	    showRstudio.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
			    Frame iframe = new Frame(presenter.getRstudioUrl());
			    iframe.setHeight("500px");
			    iframe.setWidth("100%");
			    lc.remove(showRstudio);
			    lc.remove(label);
			    lc.add(iframe);
				lc.layout(true);
			}
		});	    
	    lc.add(showRstudio);
	    lc.add(label, new MarginData(5, 0, 0, 0));
		
	    lc.layout();
	    return lc;  		
	}
	
	private Widget createTitleWidget(EntityBundle bundle, String entityTypeDisplay) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		String title = "<h2><span style=\"font-weight:lighter;\">["
				+ entityTypeDisplay.substring(0, 1)
				+ "]</span> "
				+ bundle.getEntity().getName()
				+ "&nbsp;(" + bundle.getEntity().getId() + ")</h2>";
    	lc.add(new Html(title));  
		
	    // Metadata
	    lc.add(createMetadata(bundle.getEntity()));
	    // the headers for description and property
	   	  
	    lc.layout();
		return lc;
	}
		
	private Widget createDescriptionWidget(EntityBundle bundle, String entityTypeDisplay) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new Html("<h3>Description</h3>"));	

		// Add the description body
	    String description = bundle.getEntity().getDescription();
	    if(description == null || "".equals(description)) {
	    	description = "<div style=\"font-size: 80%\">" + DisplayConstants.LABEL_NO_DESCRIPTION + "</div>";
	    }
	    lc.add(new Html(description));

		
		lc.layout();
		return lc;
	}

	private Widget createPropertyWidget(EntityBundle bundle) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new Html("<h3>Properties &amp; Annotations</h3>"));  
	    
	    // Create the property body
	    // the headers for properties.
	    //propertiesHeader.add(new Html("<h4>Properties &amp; Annotations</h4>"));
	    propertyWidget.setEntityBundle(bundle);
	    lc.add(propertyWidget.asWidget());
	    lc.layout();
		return lc;
	}	
	
	private Widget createAttachmentsWidget(EntityBundle bundle) {
		LayoutContainer lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(new Html("<h3>Attachments</h3>"));	

		ContentPanel cp = new ContentPanel();
		cp.setHeight(150);
		cp.setHeaderVisible(false);
		cp.add(new Html("Coming soon."), new MarginData(5));
		lc.add(cp);
		
		lc.layout();
		return lc;
	}

	
}
