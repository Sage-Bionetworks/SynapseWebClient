package org.sagebionetworks.web.client.widget;

public enum CheckBoxState {
	SELECTED, DESELECTED, INDETERMINATE;

	public static CheckBoxState getStateFromCount(int currentlySelected, int total) {
		CheckBoxState state;
		if (currentlySelected <= 0) {
			state = DESELECTED;
		} else if (currentlySelected == total) {
			state = SELECTED;
		} else {
			state = INDETERMINATE;
		}
		return state;
	}
}
