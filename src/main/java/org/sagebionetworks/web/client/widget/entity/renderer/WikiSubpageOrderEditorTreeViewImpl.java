package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpageOrderEditorTree.SubpageOrderEditorTreeNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpageOrderEditorTreeViewImpl extends FlowPanel implements WikiSubpageOrderEditorTreeView {

	private Presenter presenter;

	private Map<String, Widget> headerId2listItem;
	private Map<Widget, UnorderedListPanel> listItem2childrenList;
	private IsWidget synAlert;
	String ownerObjectId;

	@Inject
	public WikiSubpageOrderEditorTreeViewImpl() {
		addStyleName("notopmargin nav bs-sidenav well wiki-subpages-editor-tree");
		add(new HTML("<h4 class=\"margin-left-15\">Edit Order</h4>"));

		headerId2listItem = new HashMap<String, Widget>();
		listItem2childrenList = new HashMap<Widget, UnorderedListPanel>();
	}

	public void moveTreeItem(SubpageOrderEditorTreeNode node, boolean moveUp) {
		Widget listItem = headerId2listItem.get(node.getHeader().getId());
		UnorderedListPanel childrenList = listItem2childrenList.get(headerId2listItem.get(node.getHeader().getId()));


		SubpageOrderEditorTreeNode parent = presenter.getParent(node);
		UnorderedListPanel parentPanel = listItem2childrenList.get(headerId2listItem.get(node.getHeader().getParentId()));

		int insertIndex;
		if (moveUp) {
			SubpageOrderEditorTreeNode sibling = parent.getChildren().get(parent.getChildren().indexOf(node) - 1);
			Widget siblingListItem = headerId2listItem.get(sibling.getHeader().getId());
			insertIndex = parentPanel.indexOf(siblingListItem);
		} else {
			SubpageOrderEditorTreeNode sibling = parent.getChildren().get(parent.getChildren().indexOf(node) + 1);
			Widget siblingListItem = headerId2listItem.get(sibling.getHeader().getId());
			insertIndex = parentPanel.indexOf(siblingListItem) + 1;
			if (listItem2childrenList.containsKey(siblingListItem)) {
				// There is also an unordered list that the node should be moved past.
				insertIndex++;
			}
		}

		// Add to insertIndex.

		swapWidgetInPanel(parentPanel, listItem, insertIndex, moveUp);

		int childListIndex = parentPanel.indexOf(listItem) + 1;

		if (childrenList != null) {
			swapWidgetInPanel(parentPanel, childrenList, childListIndex, moveUp);
		}


	}

	private void swapWidgetInPanel(UnorderedListPanel parentPanel, Widget toInsert, int insertIndex, boolean moveUp) {
		boolean willBeLastItem = insertIndex >= parentPanel.getWidgetCount();
		parentPanel.remove(toInsert);
		if (moveUp)
			parentPanel.insert(toInsert, insertIndex);
		else {
			if (willBeLastItem) {
				parentPanel.add(toInsert);
			} else {
				parentPanel.insert(toInsert, insertIndex - 1); // subtract 1 since removed
			}
		}
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
	public void configure(SubpageOrderEditorTreeNode overallRoot, String ownerObjectId) {
		this.ownerObjectId = ownerObjectId;
		UnorderedListPanel rootPanel = new UnorderedListPanel();
		rootPanel.addStyleName("notopmargin nav bs-sidenav margin-bottom-10");
		addTreeItemsRecursive(rootPanel, overallRoot);
		this.add(rootPanel);
		add(synAlert);
	}

	private void addTreeItemsRecursive(UnorderedListPanel ul, SubpageOrderEditorTreeNode root) {
		Widget listItem = makeListItem(root);
		ul.add(listItem);
		headerId2listItem.put(root.getHeader().getId(), listItem);
		if (!root.getChildren().isEmpty()) {
			UnorderedListPanel subList = new UnorderedListPanel();
			listItem2childrenList.put(listItem, subList);
			subList.addStyleName("nav");
			ul.add(subList);
			for (SubpageOrderEditorTreeNode child : root.getChildren()) {
				addTreeItemsRecursive(subList, child);
			}
		}
	}

	public void selectTreeItem(SubpageOrderEditorTreeNode toSelect) {
		if (toSelect == null)
			throw new IllegalArgumentException("Node to select cannot be null");
		// Remove style from previously selected list item.
		SubpageOrderEditorTreeNode prevSelected = presenter.getSelectedTreeItem();
		if (prevSelected != null) {
			UnorderedListPanel.removeStyleNameFromListItem(headerId2listItem.get(prevSelected.getHeader().getId()), "active");
		}

		// Add style to the selecting list item.
		UnorderedListPanel.addStyleNameToListItem(headerId2listItem.get(toSelect.getHeader().getId()), "active");
		headerId2listItem.get(toSelect.getHeader().getId()).getElement().focus();
	}

	private Widget makeListItem(final SubpageOrderEditorTreeNode node) {
		final Anchor l = new Anchor(node.getText());
		l.addStyleName("link");
		l.setHref("#!Synapse:" + new Synapse(ownerObjectId, null, EntityArea.WIKI, node.getHeader().getId()).toToken());
		l.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isAnyModifierKeyDown(event)) {
					event.preventDefault();
					presenter.selectTreeItem(node);
				}
			}
		});
		return l;
	}

	@Override
	public void setSynAlert(IsWidget w) {
		this.synAlert = w;
	}
}
