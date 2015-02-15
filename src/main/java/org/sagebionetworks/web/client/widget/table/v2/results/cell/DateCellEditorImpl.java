package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;

import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellEditorImpl implements DateCellEditor {
	
	private DateCellEditorView view;
	
	@Inject
	public DateCellEditorImpl(DateCellEditorView view) {
		this.view = view;
	}

	@Override
	public boolean isValid() {
		// The editor will not allow bad values.
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		value = StringUtils.trimWithEmptyAsNull(value);
		Date date = null;
		if(value != null){
			date = new Date(Long.parseLong(value));
		}
		view.setValue(date);
	}

	@Override
	public String getValue() {
		Date date = view.getValue();
		if(date != null){
			return Long.toString(date.getTime());
		}
		return null;
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return view.addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		view.fireEvent(event);
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

}
