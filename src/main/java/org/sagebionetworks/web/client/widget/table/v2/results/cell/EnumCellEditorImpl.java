package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.List;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EnumCellEditorImpl implements EnumCellEditor {


	EnumCellEditorView view;
	
	@Inject
	public EnumCellEditorImpl(EnumCellEditorView view){
		this.view = view;
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
		view.setValue(value);
	}

	@Override
	public String getValue() {
		return view.getValue();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// Cannot hanlde this.
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

	@Override
	public void configure(List<String> validValues) {
		// Add each value to the select
		for(String value: validValues){
			this.view.addOption(value);
		}
	}

}
