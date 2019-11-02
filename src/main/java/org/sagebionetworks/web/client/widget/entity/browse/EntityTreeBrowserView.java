package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;
import org.sagebionetworks.web.client.widget.entity.MoreTreeItem;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityTreeBrowserView extends IsWidget, SynapseView {
	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Rather than linking to the Entity Page, a clicked entity in the tree will become selected.
	 */
	void makeSelectable();

	void clearSelection();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onToggleSort(SortBy sortColumn);

		void setSelection(String id);

		void expandTreeItemOnOpen(final EntityTreeItem target);

		void clearRecordsFetchedChildren();

		void addMoreButton(MoreTreeItem moreItem, String parentId, EntityTreeItem parent, String nextPageToken);

		void getChildren(String parentId, EntityTreeItem parent, String nextPageToken);

		void copyIDsToClipboard();
	}

	void appendRootEntityTreeItem(EntityTreeItem childToAdd);

	void appendChildEntityTreeItem(EntityTreeItem childToAdd, EntityTreeItem parent);

	void configureEntityTreeItem(EntityTreeItem childToAdd);

	void placeChildMoreTreeItem(MoreTreeItem childToCreate, EntityTreeItem parent, String nextPageToken);

	void placeRootMoreTreeItem(MoreTreeItem childToCreate, String parentId, String nextPageToken);

	void showEmptyUI();

	int getRootCount();

	void hideEmptyUI();

	void setLoadingVisible(boolean isShown);

	void setSynAlert(IsWidget w);

	void setSortable(boolean isSortable);

	void showMinimalColumnSet();

	void clearSortUI();

	void setSortUI(SortBy sortBy, Direction dir);

	void copyToClipboard(String value);
}
