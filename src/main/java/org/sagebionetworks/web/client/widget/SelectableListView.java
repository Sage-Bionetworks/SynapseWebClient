package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.widget.IndeterminateCheckBox.CheckBoxState;

public interface SelectableListView {
	//selection toolbar state
	void setCanDelete(boolean canDelete);
	void setCanMoveUp(boolean canMoveUp);
	void setCanMoveDown(boolean canMoveDown);
	void setButtonToolbarVisible(boolean visible);
	void setSelectionToolbarHandler(Presenter selectableItemList);
	void setSelectionState(CheckBoxState selectionState);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onMoveDown();
		void onMoveUp();
		void deleteSelected();
		void selectNone();
		void selectAll();
	}
}
