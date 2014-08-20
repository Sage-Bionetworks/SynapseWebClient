package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Tree.Resources;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends FlowPanel implements EntityTreeBrowserView {

	public static final String EMPTY_DISPLAY = "&#8212" + " " + DisplayConstants.EMPTY;
	
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private IconsImageBundle iconsImageBundle;
		
	private boolean makeLinks = true;		// TODO: Default true?
	private Tree entityTree;
	private Map<TreeItem, EntityTreeItem> treeItem2entityTreeItem;
	private Map<EntityHeader, EntityTreeItem> header2entityTreeItem;	// for removing
	private EntityTreeItem selectedItem;		// TODO: Why do I have to implement this?
	

	@Inject
	public EntityTreeBrowserViewImpl(PortalGinInjector ginInjector, IconsImageBundle iconsImageBundle) {
		this.ginInjector = ginInjector;
		this.iconsImageBundle = iconsImageBundle;
		treeItem2entityTreeItem = new HashMap<TreeItem, EntityTreeItem>();
		header2entityTreeItem = new HashMap<EntityHeader, EntityTreeItem>();
		
		entityTree = new Tree(new EntityTreeResources());
		
		this.add(entityTree);
		entityTree.addOpenHandler(new OpenHandler<TreeItem>() {
			
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				final EntityTreeItem target = treeItem2entityTreeItem.get(event.getTarget());
				presenter.expandTreeItemOnOpen(target);
			}
			
		});
		
		entityTree.addSelectionHandler(new SelectionHandler<TreeItem>() {

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				if (selectedItem != null) {
					selectedItem.asTreeItem().removeStyleName("entityTreeItem-selected");
				}
				final EntityTreeItem targetItem = treeItem2entityTreeItem.get(event.getSelectedItem());
				selectedItem = targetItem;
				targetItem.asTreeItem().addStyleName("entityTreeItem-selected");
				presenter.setSelection(selectedItem.getHeader().getId());
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
		entityTree.clear();
		treeItem2entityTreeItem.clear();
		header2entityTreeItem.clear();
		presenter.clearRecordsFetchedChildren();
	}

	@Override
	public void setRootEntities(List<EntityHeader> rootEntities, boolean sort) {
		clear();
		
		if(rootEntities == null) rootEntities = new ArrayList<EntityHeader>();
		if(rootEntities.size() == 0) {
			TreeItem emptyItem = new TreeItem(new HTMLPanel("<div>" + EMPTY_DISPLAY + "</div>"));
			emptyItem.addStyleName("entityTreeItem-font");
			entityTree.addItem(emptyItem);
		} else {
			for (final EntityHeader header : rootEntities) {
				createAndPlaceRootTreeItem(header);
			}
		}
	}
	
	@Override
	public void setMakeLinks(boolean makeLinks) {
		this.makeLinks = makeLinks;
	}
	
	/*
	 * Private Methods
	 */

	@Override
	public void removeEntity(EntityHeader entityHeader) {
		header2entityTreeItem.get(entityHeader).asTreeItem().remove();
	}
	
	@Override
	public void createAndPlaceTreeItem(EntityHeader childToCreate, EntityTreeItem parent, boolean isRootItem) {
		if (parent == null && !isRootItem) throw new IllegalArgumentException("Must specify a parent entity under which to place the created child in the tree.");
		
		// Make tree item.
		EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.setMakeLinks(makeLinks);
		childItem.configure(childToCreate, isRootItem);
		
		// Update fields.
		treeItem2entityTreeItem.put(childItem.asTreeItem(), childItem);
		header2entityTreeItem.put(childToCreate,  childItem);
				
		// Add dummy item to childItem to make expandable.
		childItem.asTreeItem().addItem(createDummyItem());
		
		// Place the created child in the tree as the child of the given parent entity.
		if (isRootItem) {
			entityTree.addItem(childItem);
		} else {
			parent.asTreeItem().addItem(childItem);
		}
	}
	
	@Override
	public void createAndPlaceRootTreeItem(EntityHeader toCreate) {
		createAndPlaceTreeItem(toCreate, null, true);
	}
	
	/**
	 * Creates the dummy item used to make a tree item expandable.
	 * @return the dummy tree item.
	 */
	private TreeItem createDummyItem() {
		TreeItem result = new TreeItem();
		result.setVisible(false);
		return result;
	}
	
	
	/*
	 * Image Resources for Tree Expand/Collapse Icons
	 */
	
	public class EntityTreeResources implements Tree.Resources {
		@Override
	    public ImageResource treeClosed() {
	        return iconsImageBundle.arrowRightDir16();
	    }

	    @Override
	    public ImageResource treeOpen() {
	        return iconsImageBundle.arrowDownDir16();
	    }

		@Override
		public ImageResource treeLeaf() {
			return EntityTreeImageBundle.DEFAULT_RESOURCES.treeLeaf();
		}
	}
	
	public interface EntityTreeImageBundle extends ClientBundle, ClientBundleWithLookup {
		Tree.Resources DEFAULT_RESOURCES = GWT.create(Tree.Resources.class);
	}
	
}
