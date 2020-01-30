package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EnumCellEditor implements CellEditor {

	public static final String NOTHING_SELECTED = "nothing selected";

	ListCellEdtiorView view;
	SynapseJSNIUtils jsniUtils;
	ArrayList<String> items;

	@Inject
	public EnumCellEditor(ListCellEdtiorView view, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.jsniUtils = jsniUtils;
	}

	@Override
	public boolean isValid() {
		// The widget will not allow invalid values.
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		value = StringUtils.emptyAsNull(value);
		if (value == null) {
			// null means the first item: 'nothing selected'
			view.setValue(0);
			return;
		}
		/*
		 * Find the index matching the value. Note: Linear search for less than 100 items is reasonable.
		 */
		for (int i = 0; i < items.size(); i++) {
			if (value.equals(items.get(i))) {
				view.setValue(i);
				return;
			}
		}

		// we did not match a value, invalid value given
		view.setValue(0);
		jsniUtils.consoleError("Unknown value: " + value);
		return;
	}

	@Override
	public String getValue() {
		int index = view.getValue();
		if (index == 0) {
			// The first item 'nothing selected' means null.
			return null;
		} else {
			return this.items.get(index);
		}
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// Cannot handle this.
	}

	@Override
	public int getTabIndex() {
		return view.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		view.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		view.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		view.setTabIndex(index);
	}

	public void configure(List<String> validValues) {
		/*
		 * The items include all passed values plus the first item is 'nothing selected' for null and empty
		 * values.
		 */
		this.items = new ArrayList<String>(validValues.size() + 1);
		this.items.add(NOTHING_SELECTED);
		for (String value : validValues) {
			this.items.add(value);
		}
		view.configure(this.items);
	}

}
