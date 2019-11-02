package org.sagebionetworks.web.client.widget;

public interface SelectableListItem {
	boolean isSelected();

	void setSelected(boolean selected);

	public interface Presenter {
		void onSelectionChanged();
	}
}
