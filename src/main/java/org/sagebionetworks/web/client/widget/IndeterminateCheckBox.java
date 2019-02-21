package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.CheckBox;

public class IndeterminateCheckBox extends CheckBox {
	public static final String INDETERMINATE_STYLE = "indeterminate-checkbox";
	public enum CheckBoxState {
		SELECTED, 
		DESELECTED,
		INDETERMINATE;
	}
	public void setState(CheckBoxState state) {
		switch (state) {
		case SELECTED:
			removeStyleName(INDETERMINATE_STYLE);
			setValue(true);
			break;
		case DESELECTED:
			removeStyleName(INDETERMINATE_STYLE);
			setValue(false);
			break;
		case INDETERMINATE:
			addStyleName(INDETERMINATE_STYLE);
			setValue(false);
			break;
		default:
			break;
		}
	}
	
	public CheckBoxState getState() {
		if (getStyleName().contains(INDETERMINATE_STYLE)) {
			return CheckBoxState.INDETERMINATE;
		} else if (getValue()) {
			return CheckBoxState.SELECTED;
		} else {
			return CheckBoxState.DESELECTED;
		}
	}
	
	public static CheckBoxState getStateFromCount(int currentlySelected, int total) {
		CheckBoxState state;
		if (currentlySelected == 0) {
			state = CheckBoxState.DESELECTED;
		} else if (currentlySelected == total) {
			state = CheckBoxState.SELECTED;
		} else {
			state = CheckBoxState.INDETERMINATE;
		}
		return state;
	}
}
