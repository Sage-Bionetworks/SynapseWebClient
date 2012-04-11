package org.sagebionetworks.web.client.widget.entity.children;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.LocationData;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.events.EntitySelectedEvent;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowser;
import org.sagebionetworks.web.client.widget.statictable.StaticTable;
import org.sagebionetworks.web.client.widget.statictable.StaticTableColumn;
import org.sagebionetworks.web.client.widget.statictable.StaticTableView;
import org.sagebionetworks.web.client.widget.statictable.StaticTableViewImpl;
import org.sagebionetworks.web.client.widget.table.QueryTableFactory;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.WhereCondition;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityChildBrowserViewImpl extends LayoutContainer implements
		EntityChildBrowserView {
	
	private static final String KEY_TARGET_VERSION_NUMBER = "targetVersionNumber";
	private static final String KEY_TARGET_ID = "targetId";
	private static final String KEY_TYPE = "type";
	private static final String KEY_NAME= "name";
	private static final String KEY_REFERENCE_URI = "referenceUri";
	private final static int MAX_IMAGE_PREVIEW_WIDTH_PX = 800;
	private static final int BASE_PANEL_HEIGHT_PX = 270;
	private static final int TALL_PANEL_HEIGHT_PX = 500;
	private static final int SLIDER_HEIGHT_PX = 25;
	private static final int MAX_NUMBER_PREVIEWTABLE_COLS = 35;
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private TabPanel tabPanel;
	private QueryTableFactory queryTableFactory;
	private ColumnFactory columnFactory;
	private EntityTreeBrowser entityTreeBrowser;
	private EntityTypeProvider entityTypeProvider;
	
	private TabItem previewTab;
	private ContentPanel previewLoading;
	private int tabPanelHeight;
	private boolean addStaticTable;
	private boolean imagePreviewExpanded;
	private Integer originalImageWidth;
	private Integer originalImageHeight;
	private LayoutContainer lc;

	@Inject
	public EntityChildBrowserViewImpl(SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle,
			QueryTableFactory queryTableFactory, ColumnFactory columnFactory,
			EntityTreeBrowser entityTreeBrowser, EntityTypeProvider entityTypeProvider) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.queryTableFactory = queryTableFactory;
		this.columnFactory = columnFactory;
		this.entityTreeBrowser = entityTreeBrowser;
		this.entityTypeProvider = entityTypeProvider;
		
		this.setLayout(new FitLayout());
	}

	@Override
	public void createBrowser(final Entity entity, EntityType entityType,
			boolean canEdit) {
		if(lc != null && tabPanel != null) {
			// out with the old
			this.remove(lc);
			tabPanel.removeAll();
		}		
		tabPanel = new TabPanel();
		tabPanel.setLayoutData(new FitLayout());
		tabPanel.setPlain(true);
		tabPanel.setAutoWidth(true);
		tabPanel.setAnimScroll(true);  
		tabPanel.setTabScroll(true);  
		// determine tabPanel height
		final LocationData location = presenter.getMediaLocationData();
		if(location != null && location.getPath() != null) {
			tabPanelHeight = TALL_PANEL_HEIGHT_PX;
		} else {
			tabPanelHeight = BASE_PANEL_HEIGHT_PX;
		}
		tabPanel.setHeight(tabPanelHeight);
		
		int numAdded = 0;
		
		// add tabs
		numAdded += addTreeviewTab(entity);
		numAdded += addChildTabs(entityType, location); 		
		
		if(numAdded == 0) {
			final TabItem tab = new TabItem("None");			
			tab.addStyleName("pad-text");											
			Html noChildren = new Html(DisplayConstants.LABEL_CONTAINS_NO_CHILDREN);
			tab.add(noChildren, new MarginData(10));
			tabPanel.add(tab);  
		}

		lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);
		lc.add(tabPanel);
		lc.add(new Html(DisplayUtils.getIconHtml(iconsImageBundle
				.informationBalloon16())
				+ " <span style=\"font-size: 80%\">"
				+ DisplayConstants.TEXT_RIGHT_CLICK_FOR_CONTEXT_MENU + "</span>"));
		lc.layout();
		add(lc);
	}

	private int addTreeviewTab(Entity entity) {
		int numAdded = 0;		
		final TabItem tab = new TabItem("All Contents");
		tab.setScrollMode(Scroll.AUTO);
		tab.addStyleName("pad-text");			
		final ContentPanel loading = DisplayUtils.getLoadingWidget(sageImageBundle);
		loading.setHeight(tabPanelHeight);		
		tab.add(loading);
		// Render only if selected
		tab.addListener(Events.Render, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				presenter.getChildrenHeaders(new AsyncCallback<List<EntityHeader>>() {
					@Override
					public void onSuccess(List<EntityHeader> result) {
						tab.remove(loading);
						entityTreeBrowser.setRootEntities(result);
						entityTreeBrowser.addEntityUpdatedHandler(new EntityUpdatedHandler() {							
							@Override
							public void onPersistSuccess(EntityUpdatedEvent event) {
								presenter.reloadEntity();
							}
						});
						tab.add(entityTreeBrowser.asWidget());
						tab.layout(true);						
					}
					@Override
					public void onFailure(Throwable caught) {
						tab.remove(loading);
						tab.add(new Html(DisplayConstants.ERROR_GENERIC_RELOAD), new MarginData(10));
						tab.layout(true);						
					}
				});
			}
		});
		tabPanel.add(tab);	
		numAdded++;
		
		return numAdded;
	}
	
	private int addChildTabs(EntityType entityType,
			final LocationData location) {
		int numAdded=0;
		List<EntityType> skipTypes = presenter.getContentsSkipTypes();
		List<EntityType> children = new ArrayList<EntityType>();
		children.addAll(entityType.getValidChildTypes());
		if(children != null && children.size() > 0) {
			// fill map
			Map<String,EntityType> classToTypeMap = new HashMap<String, EntityType>();
			for(int i=0; i<children.size(); i++) {
				EntityType child = children.get(i);
				if(skipTypes.contains(child)) {
					children.remove(child);
					continue; // skip some types
				}
				classToTypeMap.put(child.getClassName(), child);
			}
			 
			// add child tabs in order
			for(String className : DisplayUtils.ENTITY_TYPE_DISPLAY_ORDER) {
				if(classToTypeMap.containsKey(className)) {
					EntityType child = classToTypeMap.get(className);
					children.remove(child);
					tabPanel.add(createChildTab(child, location));	
					numAdded++;					
				}
			}

			// add any remaining tabs that weren't covered by the display order
			for(final EntityType child : children) {				
				tabPanel.add(createChildTab(child, location));	
				numAdded++;
			}
		}
		return numAdded;
	}

	private TabItem createChildTab(final EntityType child, final LocationData location) {
		final String childDisplay = entityTypeProvider.getEntityDispalyName(child);				
		final TabItem tab = new TabItem(childDisplay);			
		tab.addStyleName("pad-text");			
		final ContentPanel loading = DisplayUtils.getLoadingWidget(sageImageBundle);
		loading.setHeight(tabPanelHeight);		
		tab.add(loading);
		tab.addListener(Events.Render, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if("preview".equals(child.getName())) {									
					// let presenter create preview info when data is loaded
					previewTab = tab;
					previewLoading = loading;
					
					// Synchronous previews						
					if(location != null && location.getPath() != null) {
						setMediaPreview(location);
					}
					
				} else {
					// loading is embedded into the query table widget
					addQueryTable(child, tab, loading);							
				}
			}
		});
		return tab;
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
	public void setPreviewTable(PreviewData previewData) {
		if(previewData == null) previewData = new PreviewData();
		// add static table view of preview		
		List<Map<String, String>> rows = previewData.getRows();
		Map<String, String> columnUnits = previewData.getColumnUnits();
		Map<String, String> columnDescriptions = previewData
				.getColumnDescriptions();
		List<String> columnDisplayOrder = previewData.getColumnDisplayOrder();
		
		if(previewTab == null)
			return;		
		
		previewTab.removeAll();
		if (rows != null && rows.size() > 0 && columnDescriptions != null
				&& columnUnits != null && columnDisplayOrder != null) {
			// create static table columns
			StaticTableView view = new StaticTableViewImpl();
			StaticTable staticTable = new StaticTable(view);
			staticTable.setHeight(tabPanelHeight-SLIDER_HEIGHT_PX); // sub slider height
			staticTable.setShowTitleBar(false);

			List<StaticTableColumn> stColumns = new ArrayList<StaticTableColumn>();
			int ncol = 0;
			for (String key : previewData.getColumnDisplayOrder()) {
				if(ncol >= MAX_NUMBER_PREVIEWTABLE_COLS) break;
				StaticTableColumn stCol = new StaticTableColumn();
				stCol.setId(key);
				stCol.setName(key);

				// add units to column if available
				if (columnUnits.containsKey(key)) {
					stCol.setUnits(columnUnits.get(key));
				}

				// add description if available
				if (columnDescriptions.containsKey(key)) {
					stCol.setTooltip(columnDescriptions.get(key));
				}

				stColumns.add(stCol);
				ncol++;
			}
			
			staticTable.setDataAndColumnsInOrder(previewData.getRows(),
					stColumns);
			previewTab.add(staticTable.asWidget());
		} else {			
			setNoPreview();
		}
		
		previewTab.layout(true);
	}
		
	private void setMediaPreview(LocationData locationData) {
		if(previewTab == null)
			return;		
		
		previewTab.removeAll();
		if(locationData != null) {
			// handle image preview
			final ContentPanel cp = new ContentPanel();						
			cp.setHeaderVisible(false);			
			cp.setAutoWidth(true);
			cp.setHeight(tabPanelHeight - SLIDER_HEIGHT_PX);			
			cp.setScrollMode(Scroll.ALWAYS);
			
			final Image previewImageWidget = new Image(locationData.getPath());
			imagePreviewExpanded = false; // start collapsed
			
			final Button expandImagePreviewButton = new Button("View Full Size", AbstractImagePrototype.create(iconsImageBundle.magnifyZoomIn16()));
			expandImagePreviewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					if(imagePreviewExpanded) {
						previewImageWidget.setWidth(MAX_IMAGE_PREVIEW_WIDTH_PX + "px");							
						previewImageWidget.setHeight(originalImageHeight * MAX_IMAGE_PREVIEW_WIDTH_PX / originalImageWidth + "px");
						imagePreviewExpanded = false;
						expandImagePreviewButton.setText("View Full Size");
						expandImagePreviewButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.magnifyZoomIn16()));
					} else {
						previewImageWidget.setHeight(originalImageHeight + "px");
						previewImageWidget.setWidth(originalImageWidth + "px");
						imagePreviewExpanded = true;
						expandImagePreviewButton.setText("Zoom Out");
						expandImagePreviewButton.setIcon(AbstractImagePrototype.create(iconsImageBundle.magnifyZoomOut16()));
					}
				}
			});

			final ToolBar toolbar = new ToolBar();

			// scale image if it needs it
			previewImageWidget.addLoadHandler(new LoadHandler() {					
				@Override
				public void onLoad(LoadEvent event) {
					if(originalImageHeight == null && originalImageWidth == null) {							
						originalImageHeight = previewImageWidget.getHeight();
						originalImageWidth = previewImageWidget.getWidth();
					}						
					if(originalImageHeight > MAX_IMAGE_PREVIEW_WIDTH_PX) {
						// scale image
						previewImageWidget.setWidth(MAX_IMAGE_PREVIEW_WIDTH_PX + "px");							
						previewImageWidget.setHeight(originalImageHeight * MAX_IMAGE_PREVIEW_WIDTH_PX / originalImageWidth + "px");
						imagePreviewExpanded = false;
						toolbar.add(expandImagePreviewButton);							
					}
				}
			});

			
			cp.setTopComponent(toolbar);
			cp.add(previewImageWidget);
			previewTab.add(cp);
			cp.layout(true);
		} else {
			setNoPreview();
		}
		previewTab.layout(true);
	}

	
	/*
	 * Private Methods
	 */
	private void addQueryTable(final EntityType child,
			final TabItem tab, final ContentPanel loading) {
		// add query table
		queryTableFactory.createColumnModel(child, new AsyncCallback<ColumnModel>() {
			@Override
			public void onSuccess(ColumnModel cm) {					   		        
				// limit view to just this entity's children
 				final List<WhereCondition> where = presenter.getProjectContentsWhereContidions();
				ContentPanel cp = queryTableFactory.createGridPanel(child, cm, where, null);
		        if(cp != null && cp.getElement() != null) {
		        	cp.setHeight(tabPanelHeight-25);
		        	cp.setHeaderVisible(false);
					tab.remove(loading);
					tab.add(cp);
					tab.layout(true);
		        } else {
		        	onFailure(null);
		        }
			}
			
			@Override
			public void onFailure(Throwable caught) {
				tab.remove(loading);
				tab.add(new Html(DisplayConstants.ERROR_GENERIC_RELOAD), new MarginData(10));
				tab.layout(true);
			}
		});
	}

	private void setNoPreview() {
		// no data in preview
		previewTab.add(new Html(DisplayConstants.LABEL_NO_PREVIEW_DATA),
				new MarginData(10));
		previewTab.layout(true);
	}

	private GridCellRenderer<BaseModelData> configureShortcutGridCellRenderer() {
		// configure cell renderer
		GridCellRenderer<BaseModelData> cellRenderer = new GridCellRenderer<BaseModelData>() {
			public String render(BaseModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				String id = model.get(KEY_TARGET_ID);
				if(model.get(KEY_TARGET_VERSION_NUMBER) != null)
					id += "." + model.get(KEY_TARGET_VERSION_NUMBER);

				return "<a href=\"" + model.get(KEY_REFERENCE_URI) + "\">" + DisplayUtils.getIconHtml(iconsImageBundle.documentArrow16()) + model.get(KEY_NAME) +" (" + id + ")</a>";
			}
		};
		return cellRenderer;
	}			
		
}

