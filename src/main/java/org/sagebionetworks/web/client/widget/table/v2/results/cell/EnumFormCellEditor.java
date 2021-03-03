package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.view.DivView;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EnumFormCellEditor implements CellEditor {

	public static final String NOTHING_SELECTED = "nothing selected";

	ListCellEditorView listView;
	RadioCellEditorView radioView;
	PortalGinInjector ginInjector;
	ArrayList<String> items;
	DivView view;
	public static final int MAX_RADIO_BUTTONS = 10;

	@Inject
	public EnumFormCellEditor(DivView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
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
			return;
		}
		/*
		 * Find the index matching the value. Note: Linear search for less than 100 items is reasonable.
		 */
		if (radioView != null) {
			for (int i = 0; i < items.size(); i++) {
				if (value.equals(items.get(i))) {
					radioView.setValue(i);
					return;
				}
			}
		} else {
			for (int i = 0; i < items.size(); i++) {
				if (value.equals(items.get(i))) {
					listView.setValue(i);
					return;
				}
			}
		}

		// If here we did not match a value
		throw new IllegalArgumentException("Unknown value: " + value);
	}

	@Override
	public String getValue() {
		Integer index = null;
		if (radioView != null) {
			index = radioView.getValue();
		} else {
			index = listView.getValue();
		}
		if (index == null) {
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
		if (radioView != null) {
			return radioView.getTabIndex();
		} else {
			return listView.getTabIndex();
		}
	}

	@Override
	public void setAccessKey(char key) {
		if (radioView != null) {
			radioView.setAccessKey(key);
		} else {
			listView.setAccessKey(key);
		}
	}

	@Override
	public void setFocus(boolean focused) {
		if (radioView != null) {
			radioView.setFocus(focused);
		} else {
			listView.setFocus(focused);
		}
	}

	@Override
	public void setTabIndex(int index) {
		if (radioView != null) {
			radioView.setTabIndex(index);
		} else {
			listView.setTabIndex(index);
		}
	}

	public void configure(List<String> validValues) {
		view.clear();
		this.items = new ArrayList<String>(validValues.size());
		for (String value : validValues) {
			this.items.add(value);
		}
		if (validValues.size() > MAX_RADIO_BUTTONS) {
			radioView = null;
			listView = ginInjector.createListCellEditorView();
			listView.configure(this.items);
			view.add(listView);
		} else {
			listView = null;
			radioView = ginInjector.createRadioCellEditorView();
			radioView.configure(this.items);
			view.add(radioView);
		}
	}

}
