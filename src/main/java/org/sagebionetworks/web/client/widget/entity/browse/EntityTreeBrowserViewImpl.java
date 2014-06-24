package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends LayoutContainer implements EntityTreeBrowserView {

	private static final String PLACEHOLDER_ID = "-1";
	private static final String PLACEHOLDER_TYPE = "-1";
	public static final String PLACEHOLDER_NAME_PREFIX = "&#8212";
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
		
	private TreeLoader<EntityTreeModel> loader;  
	private TreePanel<EntityTreeModel> tree;  
	private ContentPanel cp;
	private TreeStore<EntityTreeModel> store;
	private HashMap<String, ImageResource> typeToIcon = new HashMap<String, ImageResource>();
	private boolean makeLinks = true;
	private Integer height = null; 
	
	@Override
	protected void onRender(com.google.gwt.user.client.Element parent, int index) {
		super.onRender(parent, index);		
		
		if(store == null) createStore();
		
	    tree = new TreePanel<EntityTreeModel>(store);  
	    tree.setStateful(true);  
	    tree.setDisplayProperty(EntityTreeModel.KEY_LINK); 
	    tree.setBorders(false);
	    
	    // statefull components need a defined id  
	    tree.setId("statefullasyncentitytree_" + (Math.random()*100));  
		tree.setIconProvider(new ModelIconProvider<EntityTreeModel>() {
			public AbstractImagePrototype getIcon(EntityTreeModel model) {
				String type = model.getType();
				ImageResource icon;
				if(typeToIcon.containsKey(type)) {
					icon = typeToIcon.get(type);
				} else {
					icon = presenter.getIconForType(type);
					typeToIcon.put(type, icon);
				}				
				if(icon == null) {
					return null;
				}
				return AbstractImagePrototype.create(icon);
			}
		});		
		
		TreePanelSelectionModel<EntityTreeModel> sm = tree.getSelectionModel();
		sm.setSelectionMode(SelectionMode.SINGLE);
		sm.addSelectionChangedListener(new SelectionChangedListener<EntityTreeModel>() {			
			@Override
			public void selectionChanged(SelectionChangedEvent<EntityTreeModel> se) {
				setSelection(se.getSelectedItem());
			}
		});
		tree.setAutoHeight(true);
		tree.setAutoWidth(true);
				
		cp = new ContentPanel();  
	    cp.setHeaderVisible(false);  
	    cp.setLayout(new FitLayout());  
	    cp.add(tree);  
	    determineHeight();	    
	    cp.setAutoWidth(true);
	    cp.setBorders(false);
	    add(cp);  		
	};
		
	private void determineHeight() {
		if(cp != null) {
			if(height == null) {
				cp.setAutoHeight(true);
				cp.setScrollMode(Scroll.NONE);
			} else {
				if(isRendered()) {
					cp.setAutoHeight(false);
					cp.setHeight(height);
					cp.setScrollMode(Scroll.AUTO);
				}
			}
		}
	}

	@Inject
	public EntityTreeBrowserViewImpl(SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;

		this.setLayout(new FitLayout());
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
		if(store == null) createStore();
		store.removeAll();
	}

	@Override
	public void setRootEntities(List<EntityHeader> rootEntities, boolean sort) {
		clear();
		
		if(rootEntities == null) rootEntities = new ArrayList<EntityHeader>();
		if(rootEntities.size() == 0) {
			EntityHeader eh = new EntityHeader();
			eh.setId(PLACEHOLDER_ID);
			eh.setName(PLACEHOLDER_NAME_PREFIX + " " + DisplayConstants.EMPTY);
			eh.setType(PLACEHOLDER_TYPE);
			rootEntities.add(eh);
		}
		store.add(convertEntityHeaderToModel(rootEntities), false);
		if (sort) store.sort(EntityTreeModel.KEY_NAME, SortDir.ASC);
	}
	
	@Override
	public void setMakeLinks(boolean makeLinks) {
		this.makeLinks = makeLinks;
	}
	
	/*
	 * Private Methods
	 */

	private List<EntityTreeModel> convertEntityHeaderToModel(List<EntityHeader> headers) {
		
		List<EntityTreeModel> models = new ArrayList<EntityTreeModel>();
		int maxlimit = presenter.getMaxLimit();
		
		for(int i=0; i<headers.size() && i<maxlimit; i++) {
			EntityHeader header = headers.get(i);
			String link;
			String name = header.getName();
			if(makeLinks && !PLACEHOLDER_ID.equals(header.getId())) {
				link = "<a href=\"" + DisplayUtils.getSynapseHistoryToken(header.getId()) + "\">" + name + "</a>";
			} else {
				link = name;
			}
			models.add(new EntityTreeModel(header.getId(), name, link, header.getType()));
		}
		if(headers.size() >= maxlimit) {
			models.add(new EntityTreeModel(PLACEHOLDER_ID, PLACEHOLDER_ID, PLACEHOLDER_NAME_PREFIX + " Limited to " + maxlimit + " results" , PLACEHOLDER_TYPE));
		}
		return models;
	}

	private void createStore() {
	    // data proxy  
	    RpcProxy<List<EntityTreeModel>> proxy = new RpcProxy<List<EntityTreeModel>>() {  
			@Override
			protected void load(Object loadConfig, final AsyncCallback<List<EntityTreeModel>> callback) {
				if(loadConfig != null) {
					String entityId = ((EntityTreeModel) loadConfig).getId();
					getFolderChildren(callback, entityId);
				} else {
					callback.onFailure(null);
				}
			}
		};
	  
	    // tree loader  
	    loader = new BaseTreeLoader<EntityTreeModel>(proxy) {  
	      @Override  
	      public boolean hasChildren(EntityTreeModel parent) {
	    	if(PLACEHOLDER_ID.equals(parent.getId())) return false; // non-entity node 	    	
	        return true; // all entities could have children. Don't lookup just to answer this correctly  
	      }  
	    };  
	  
	    // trees store  
	    store = new TreeStore<EntityTreeModel>(loader);  
	    store.setKeyProvider(new ModelKeyProvider<EntityTreeModel>() {  
	      public String getKey(EntityTreeModel model) {  
	        return model.getKey();  
	      }  
	    });  
	    store.setStoreSorter(new StoreSorter<EntityTreeModel>() {  	  
	      @Override  
	      public int compare(Store<EntityTreeModel> store, EntityTreeModel m1, EntityTreeModel m2, String property) {
	    	// put placeholders at end
	    	  if(m1.getName().startsWith(PLACEHOLDER_NAME_PREFIX)) return 1;
	    	  if(m2.getName().startsWith(PLACEHOLDER_NAME_PREFIX)) return -1;
	        return m1.getName().compareToIgnoreCase(m2.getName());  
	      }  
	    });  		   
	}

	private void setSelection(EntityTreeModel selectedItem) {
		if(selectedItem != null && !PLACEHOLDER_ID.equals(selectedItem.getId())) {
			presenter.setSelection(selectedItem.getId());
		}
	}

	private void getFolderChildren(final AsyncCallback<List<EntityTreeModel>> callback, String entityId) {
		presenter.getFolderChildren(entityId, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				// convert to model data
				List<EntityTreeModel> models = convertEntityHeaderToModel(result);
				callback.onSuccess(models);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void removeEntity(EntityTreeModel entityModel) {
		store.remove(entityModel);
	}

	@Override
	public void setWidgetHeight(int height) {
		this.height = height;
		determineHeight();		
	}
	
}
