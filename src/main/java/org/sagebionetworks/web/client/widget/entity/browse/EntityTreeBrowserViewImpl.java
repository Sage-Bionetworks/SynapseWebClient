package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.HashMap;
import java.util.Map;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Hr;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends FlowPanel implements EntityTreeBrowserView {

	public static final String EMPTY_DISPLAY = "&#8212" + " " + DisplayConstants.EMPTY;

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;

	private boolean isSelectable = false;
	private Map<TreeItem, EntityTreeItem> treeItem2entityTreeItem;
	private EntityTreeItem selectedItem;

	public interface Binder extends UiBinder<Widget, EntityTreeBrowserViewImpl> {
	}

	@UiField
	Span emptyUI;
	@UiField
	Div mainContainer;
	LoadingSpinner loadingSpinner;
	Tree entityTree;
	@UiField
	Table entityTreeHeader;
	@UiField
	Hr hrUnderTableHeaders;
	@UiField
	Div synAlertContainer;
	@UiField
	SortableTableHeaderImpl nameColumnHeader;
	@UiField
	TableHeader nameColumnHeaderUnsortable;
	@UiField
	SortableTableHeaderImpl createdOnColumnHeader;
	@UiField
	TableHeader createdOnColumnHeaderUnsortable;
	@UiField
	TableHeader sizeColumnHeader;
	@UiField
	TableHeader modifiedOnColumnHeader;
	@UiField
	TableHeader idColumnHeader;
	@UiField
	TableHeader modifiedByColumnHeader;
	@UiField
	TableHeader md5ColumnHeader;
	@UiField
	TableHeader downloadColumnHeader;

	@UiField
	Icon copyIDToClipboardIcon;
	SynapseJSNIUtils jsniUtils;
	Div entityTreeContainer = new Div();
	AuthenticationController authController;
	GlobalApplicationState globalAppState;
	CookieProvider cookies;
	private Widget widget;
	boolean isShowingMinColumnSet = false;

	@Inject
	public EntityTreeBrowserViewImpl(IconsImageBundle iconsImageBundle, Binder uiBinder, AuthenticationController authController, GlobalApplicationState globalAppState, SynapseJSNIUtils jsniUtils, CookieProvider cookies) {
		this.iconsImageBundle = iconsImageBundle;
		this.authController = authController;
		this.globalAppState = globalAppState;
		this.jsniUtils = jsniUtils;
		this.cookies = cookies;
		this.widget = uiBinder.createAndBindUi(this);
		// Make sure to show this and hide the tree on empty.
		hideEmptyUI();
		nameColumnHeader.setSortingListener(headerName -> {
			presenter.onToggleSort(SortBy.NAME);
		});
		createdOnColumnHeader.setSortingListener(headerName -> {
			presenter.onToggleSort(SortBy.CREATED_ON);
		});
		copyIDToClipboardIcon.addClickHandler(event -> presenter.copyIDsToClipboard());
	}

	@Override
	public void hideEmptyUI() {
		emptyUI.setVisible(false);
		entityTreeHeader.setVisible(true);
	}

	@Override
	public void showEmptyUI() {
		emptyUI.setVisible(true);
		entityTreeHeader.setVisible(false);
	}

	@Override
	public Widget asWidget() {
		return widget;
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public int getRootCount() {
		return getTree().getItemCount();
	}

	@Override
	public void clear() {
		entityTreeContainer.clear();
		entityTree = null;
		treeItem2entityTreeItem = null;
	}

	private Map<TreeItem, EntityTreeItem> getTreeItem2entityTreeItem() {
		if (treeItem2entityTreeItem == null) {
			treeItem2entityTreeItem = new HashMap<TreeItem, EntityTreeItem>();
		}
		return treeItem2entityTreeItem;
	}

	private Tree getTree() {
		if (entityTree == null) {
			// On open, it will call expandTreeItemOnOpen, which starts a loading message.
			entityTree = new Tree(new EntityTreeResources());
			entityTree.addOpenHandler(event -> {
				final EntityTreeItem target = getTreeItem2entityTreeItem().get(event.getTarget());
				presenter.expandTreeItemOnOpen(target);
			});

			presenter.clearRecordsFetchedChildren();
			if (isSelectable) {
				entityTree.addSelectionHandler(event -> {
					EntityTreeItem targetItem = getTreeItem2entityTreeItem().get(event.getSelectedItem());
					selectEntity(targetItem);

				});
			}
			entityTreeContainer.clear();
			entityTreeContainer.add(entityTree);
			mainContainer.add(entityTreeContainer);
		}
		return entityTree;
	}

	@Override
	public void makeSelectable() {
		this.isSelectable = true;
	}

	// When empty...
	@Override
	public void appendRootEntityTreeItem(final EntityTreeItem childToAdd) {
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		getTree().addItem(childToAdd.asTreeItem());
		childToAdd.setHeight(0);
	}

	@Override
	public void configureEntityTreeItem(final EntityTreeItem childToAdd) {
		childToAdd.asTreeItem().addItem(createDummyItem());
		if (isSelectable) {
			// Add select functionality.
			ClickHandler selectClickHandler = event -> {
				selectEntity(childToAdd);
			};
			childToAdd.setModifiedByUserBadgeClickHandler(selectClickHandler);
			childToAdd.setClickHandler(selectClickHandler);
		}
		if (isShowingMinColumnSet) {
			childToAdd.showMinimalColumnSet();
		}
		// Update fields.
		getTreeItem2entityTreeItem().put(childToAdd.asTreeItem(), childToAdd);
		// Add dummy item to childItem to make expandable.
		// Pass in something to tell it to add a createDummy item for folder
		// expansion or not
		if (!childToAdd.isExpandable()) {
			childToAdd.asTreeItem().removeItems();
		}

	}

	@Override
	public void appendChildEntityTreeItem(final EntityTreeItem childToAdd, EntityTreeItem parent) {
		// (Re)move the error to presenter
		configureEntityTreeItem(childToAdd);
		// Place the created child in the tree as the child of the given parent
		// entity.
		parent.asTreeItem().addItem(childToAdd);
		childToAdd.setHeight(parent.getHeight() + 1);
	}

	/**
	 * 
	 * @param childToCreate - the button to place into the current root-level tree.
	 * @param parentId - when not adding to the parent tree item, still need the parentId to conduct
	 *        searches.
	 */
	@Override
	public void placeRootMoreTreeItem(final MoreTreeItem childToCreate, final String parentId, final String nextPageToken) {
		childToCreate.setClickHandler(event -> {
			setLoadingVisible(true);
			presenter.getChildren(parentId, null, nextPageToken);
			childToCreate.setVisible(false);
		});
		getTree().addItem(childToCreate.asTreeItem());
	}

	/**
	 * 
	 * @param childToCreate - the button to place under the passed parent.
	 * @param parent - where to place the new child, where the id can be ascertained from the header.
	 */
	@Override
	public void placeChildMoreTreeItem(final MoreTreeItem childToCreate, final EntityTreeItem parent, final String nextPageToken) {
		childToCreate.setClickHandler(event -> {
			presenter.getChildren(parent.getHeader().getId(), parent, nextPageToken);
			childToCreate.setVisible(false);
		});
		parent.asTreeItem().addItem(childToCreate.asTreeItem());
	}

	/*
	 * Private Methods
	 */

	private void selectEntity(EntityTreeItem itemToSelect) {
		clearSelection();
		selectedItem = itemToSelect;
		selectedItem.asWidget().addStyleName("entityTreeItem-selected");
		presenter.setSelection(selectedItem.getHeader().getId());
	}

	@Override
	public void clearSelection() {
		if (selectedItem != null) {
			selectedItem.asWidget().removeStyleName("entityTreeItem-selected");
		}
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

	public interface EntityTreeImageBundle extends ClientBundle, ClientBundleWithLookup {
		Tree.Resources DEFAULT_RESOURCES = GWT.create(Tree.Resources.class);
	}

	@Override
	public void setLoadingVisible(boolean isShown) {
		if (isShown && loadingSpinner == null) {
			loadingSpinner = (LoadingSpinner) DisplayUtils.getLoadingWidget();
			loadingSpinner.setAddStyleNames("center-block center");
			mainContainer.add(loadingSpinner);
		}
		if (entityTreeContainer != null)
			entityTreeContainer.setVisible(!isShown);
		if (loadingSpinner != null)
			loadingSpinner.setVisible(isShown);
		entityTreeHeader.setVisible(!isShown);
		hrUnderTableHeaders.setVisible(!isShown);
	}

	@Override
	public void showLoading() {
		setLoadingVisible(true);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void clearSortUI() {
		nameColumnHeader.setSortDirection(null);
		createdOnColumnHeader.setSortDirection(null);
	}

	@Override
	public void setSortUI(SortBy sortBy, Direction dir) {
		clearSortUI();
		SortDirection direction = Direction.ASC.equals(dir) ? SortDirection.ASC : SortDirection.DESC;
		if (SortBy.NAME.equals(sortBy)) {
			nameColumnHeader.setSortDirection(direction);
		} else if (SortBy.CREATED_ON.equals(sortBy)) {
			createdOnColumnHeader.setSortDirection(direction);
		}
	}

	@Override
	public void setSortable(boolean isSortable) {
		nameColumnHeader.setVisible(isSortable);
		nameColumnHeaderUnsortable.setVisible(!isSortable);
		if (!isShowingMinColumnSet) {
			createdOnColumnHeader.setVisible(isSortable);
			createdOnColumnHeaderUnsortable.setVisible(!isSortable);
			if (isSortable) {
				createdOnColumnHeaderUnsortable.setStyleName("");
			} else {
				createdOnColumnHeader.setStyleName("");
			}
		}
	}

	@Override
	public void copyToClipboard(String value) {
		TextArea copyToClipboardTextbox = new TextArea();
		((Div) widget).add(copyToClipboardTextbox);
		copyToClipboardTextbox.setWidth("1px");
		copyToClipboardTextbox.setFocus(true);
		copyToClipboardTextbox.setValue(value);
		copyToClipboardTextbox.selectAll();
		jsniUtils.copyToClipboard();
		((Div) widget).remove(copyToClipboardTextbox);
	}

	@Override
	public void showMinimalColumnSet() {
		this.isShowingMinColumnSet = true;
		sizeColumnHeader.setVisible(false);
		sizeColumnHeader.setStyleName("");
		modifiedOnColumnHeader.setVisible(false);
		modifiedOnColumnHeader.setStyleName("");
		// idColumnHeader.setVisible(false);
		// idColumnHeader.setStyleName("");
		createdOnColumnHeader.setVisible(false);
		createdOnColumnHeader.setStyleName("");
		createdOnColumnHeaderUnsortable.setVisible(false);
		createdOnColumnHeaderUnsortable.setStyleName("");
		// modifiedByColumnHeader.setVisible(false);
		// modifiedByColumnHeader.setStyleName("");
		md5ColumnHeader.setVisible(false);
		md5ColumnHeader.setStyleName("");
		downloadColumnHeader.setVisible(false);
		downloadColumnHeader.setStyleName("");
	}
}
