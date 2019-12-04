package org.sagebionetworks.web.client.widget;

import java.util.ArrayList;
import java.util.Iterator;
import org.sagebionetworks.web.client.utils.Callback;

public class SelectableItemList extends ArrayList<SelectableListItem> implements SelectableListView.Presenter {
	private static final long serialVersionUID = 1L;
	private boolean changingSelection = false;
	Callback refreshCallback;
	SelectableListView view;

	/**
	 * 
	 * @param refreshCallback called back when UI should be refreshed (after underlying items have been
	 *        modified).
	 * @param view view that supports list item selection (and basic operations). Typically has a
	 *        SelectionToolbar ui component.
	 */
	public void configure(Callback refreshCallback, SelectableListView view) {
		this.refreshCallback = refreshCallback;
		this.view = view;
	}

	public void selectAll() {
		changeAllSelection(true);
	}

	public void selectNone() {
		changeAllSelection(false);
	}

	public void onMoveUp() {
		int index = findFirstSelected();
		SelectableListItem item = get(index);
		remove(index);
		add(index - 1, item);
		refreshCallback.invoke();
		checkSelectionState();
	}

	public void onMoveDown() {
		int index = findFirstSelected();
		SelectableListItem item = get(index);
		remove(index);
		add(index + 1, item);
		refreshCallback.invoke();
		checkSelectionState();
	}

	public void deleteSelected() {
		Iterator<SelectableListItem> it = iterator();
		while (it.hasNext()) {
			SelectableListItem row = it.next();
			if (row.isSelected()) {
				it.remove();
			}
		}
		refreshCallback.invoke();
		checkSelectionState();
	}

	/**
	 * Find the first selected row.
	 * 
	 * @return
	 */
	public int findFirstSelected() {
		int index = 0;
		for (SelectableListItem row : this) {
			if (row.isSelected()) {
				return index;
			}
			index++;
		}
		throw new IllegalStateException("Nothing selected");
	}

	public void selectionChanged(boolean isSelected) {
		checkSelectionState();
	}

	/**
	 * Change the selection state of all rows to the passed value.
	 * 
	 * @param select
	 */
	private void changeAllSelection(boolean select) {
		try {
			changingSelection = true;
			// Select all
			for (SelectableListItem row : this) {
				row.setSelected(select);
			}
		} finally {
			changingSelection = false;
		}
		checkSelectionState();
	}

	/**
	 * The current selection state determines which buttons are enabled.
	 */
	public void checkSelectionState() {
		if (!changingSelection && view != null) {
			int index = 0;
			int count = 0;
			int lastIndex = 0;
			for (SelectableListItem row : this) {
				if (row.isSelected()) {
					count++;
					lastIndex = index;
				}
				index++;
			}
			view.setCanDelete(count > 0);
			view.setCanMoveUp(count == 1 && lastIndex > 0);
			view.setCanMoveDown(count == 1 && lastIndex < size() - 1);
			CheckBoxState state = CheckBoxState.getStateFromCount(count, size());
			view.setSelectionState(state);
		}
	}
}
