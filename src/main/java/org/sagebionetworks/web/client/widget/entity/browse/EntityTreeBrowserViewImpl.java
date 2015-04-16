package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends FlowPanel implements
		EntityTreeBrowserView {

	public static final String EMPTY_DISPLAY = "&#8212" + " "
			+ DisplayConstants.EMPTY;

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;

	private boolean isSelectable = false;
	private Tree entityTree;
	private Map<TreeItem, EntityTreeItem> treeItem2entityTreeItem;
	private EntityTreeItem selectedItem;
	private SageImageBundle sageImageBundle;
	private long offset;
	private HTML emptyUI = new HTML(EMPTY_DISPLAY);

	@Inject
	public EntityTreeBrowserViewImpl(IconsImageBundle iconsImageBundle,
			SageImageBundle sageImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		this.sageImageBundle = sageImageBundle;
		treeItem2entityTreeItem = new HashMap<TreeItem, EntityTreeItem>();
		entityTree = new Tree(new EntityTreeResources());
		this.add(entityTree);
		// On open, it will call expandTreeItemOnOpen, which starts a loading message.
		entityTree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				final EntityTreeItem target = treeItem2entityTreeItem.get(event
						.getTarget());
				presenter.expandTreeItemOnOpen(target);
			}

		});
		// Make sure to show this and hide the tree on empty.
		this.add(emptyUI);
		hideEmptyUI();
	}

	@Override
	public void hideEmptyUI() {
		emptyUI.setVisible(false);
	}

	@Override
	public void showEmptyUI() {
		emptyUI.setVisible(true);
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public int getRootCount() {
		return entityTree.getItemCount();
	}

	@Override
	public void clear() {
		entityTree.clear();
		treeItem2entityTreeItem.clear();
		presenter.clearRecordsFetchedChildren();
	}

	@Override
	public void makeSelectable() {
		this.isSelectable = true;
		entityTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				final EntityTreeItem targetItem = treeItem2entityTreeItem
						.get(event.getSelectedItem());
				selectEntity(targetItem);
			}

		});
	}

	// When empty...
	@Override
	public void appendRootEntityTreeItem(final EntityTreeItem childToAdd) {
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		entityTree.insertItem(entityTree.getItemCount() - 1, childToAdd.asTreeItem());
	}

	@Override
	public void insertRootEntityTreeItem(final EntityTreeItem childToAdd,
			long offset) {
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		entityTree.insertItem((int) offset, childToAdd.asTreeItem());
	}

	@Override
	public void configureEntityTreeItem(final EntityTreeItem childToAdd) {
		if (isSelectable) {
			// Add select functionality.
			childToAdd.setClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					selectEntity(childToAdd);
				}
			});
		}
		// Update fields.
		treeItem2entityTreeItem.put(childToAdd.asTreeItem(), childToAdd);
		// Add dummy item to childItem to make expandable.
		// Pass in something to tell it to add a createDummy item for folder
		// expansion or not
		if (childToAdd.isExpandable()) {
			childToAdd.asTreeItem().addItem(createDummyItem());
		}
	}

	@Override
	public void appendChildEntityTreeItem(final EntityTreeItem childToAdd,
			EntityTreeItem parent) {
		// (Re)move the error to presenter
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		parent.asTreeItem().addItem(childToAdd);
	}

	@Override
	public void insertChildEntityTreeItem(final EntityTreeItem childToAdd,
			EntityTreeItem parent, long offset) {
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		parent.asTreeItem().insertItem((int) offset, childToAdd.asTreeItem());
	}

	/**
	 * 
	 * @param childToCreate
	 *            - the button to place into the current root-level tree.
	 * @param parentId
	 *            - when not adding to the parent tree item, still need the
	 *            parentId to conduct searches.
	 */
	@Override
	public void placeRootMoreFoldersTreeItem(final MoreTreeItem childToCreate,
			final String parentId, final long offset) {
		childToCreate.setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Asyc or offset issues?
				presenter.getFolderChildren(parentId, null, offset,
						insertLoading(null, (int) offset));
				childToCreate.setVisible(false);
			}
		});
		entityTree.insertItem(entityTree.getItemCount() - 1, childToCreate.asTreeItem());
	}

	/**
	 * 
	 * @param childToCreate
	 *            - the button to place into the current root-level tree.
	 * @param parentId
	 *            - when not adding to the parent tree item, still need the
	 *            parentId to conduct searches.
	 */
	@Override
	public void placeRootMoreFilesTreeItem(final MoreTreeItem childToCreate,
			final String parentId, final long offset) {
		childToCreate.setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.getChildrenFiles(parentId, null, offset,
						appendLoading(null));
				childToCreate.setVisible(false);
			}
		});
		entityTree.insertItem(entityTree.getItemCount() - 1, childToCreate.asTreeItem());
	}

	/**
	 * 
	 * @param childToCreate
	 *            - the button to place under the passed parent.
	 * @param parent
	 *            - where to place the new child, where the id can be
	 *            ascertained from the header.
	 */
	@Override
	public void placeChildMoreFoldersTreeItem(final MoreTreeItem childToCreate,
			final EntityTreeItem parent, final long offset) {
		childToCreate.setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.getFolderChildren(parent.getHeader().getId(), parent,
						offset, insertLoading(parent, (int) offset));
				childToCreate.setVisible(false);
			}
		});
		parent.asTreeItem().insertItem(parent.asTreeItem().getChildCount() - 1, childToCreate.asTreeItem());
	}

	/**
	 * 
	 * @param childToCreate
	 *            - the button to place under the passed parent.
	 * @param parent
	 *            - where to place the new child, where the id can be
	 *            ascertained from the header.
	 */
	@Override
	public void placeChildMoreFilesTreeItem(final MoreTreeItem childToCreate,
			final EntityTreeItem parent, final long offset) {
		childToCreate.setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.getChildrenFiles(parent.getHeader().getId(), parent,
						offset, appendLoading(parent));
				childToCreate.setVisible(false);
			}
		});
		parent.asTreeItem().insertItem(parent.asTreeItem().getChildCount() - 1, childToCreate.asTreeItem());
	}

	/*
	 * Private Methods
	 */

	private void selectEntity(EntityTreeItem itemToSelect) {
		if (selectedItem != null) {
			selectedItem.asWidget().removeStyleName("entityTreeItem-selected");
		}
		selectedItem = itemToSelect;
		selectedItem.asWidget().addStyleName("entityTreeItem-selected");
		presenter.setSelection(selectedItem.getHeader().getId());
	}

	/**
	 * Creates the dummy item used to make a tree item expandable.
	 * 
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

	public interface EntityTreeImageBundle extends ClientBundle,
			ClientBundleWithLookup {
		Tree.Resources DEFAULT_RESOURCES = GWT.create(Tree.Resources.class);
	}

	@Override
	public void showLoading() {
	}
	
	@Override
	public void removeLoading(IsTreeItem toHide) {
		toHide.asTreeItem().remove();
	}

	@Override
	public IsTreeItem insertLoading(EntityTreeItem parent, int offset) {
		TreeItem loadingTreeItem = new TreeItem(new HTMLPanel(
				DisplayUtils.getLoadingHtml(sageImageBundle)));
		loadingTreeItem.addStyleName("entityTreeItem-font");
		if (parent == null)
			entityTree.insertItem(offset, loadingTreeItem);
		else
			parent.asTreeItem().insertItem(offset, loadingTreeItem);
		return loadingTreeItem;
	}

	@Override
	public IsTreeItem appendLoading(EntityTreeItem parent) {
		TreeItem loadingTreeItem = new TreeItem(new HTMLPanel(
				DisplayUtils.getLoadingHtml(sageImageBundle)));
		loadingTreeItem.addStyleName("entityTreeItem-font");
		if (parent == null)
			entityTree.addItem(loadingTreeItem);
		else
			parent.asTreeItem().addItem(loadingTreeItem);
		return loadingTreeItem;
	}
}
