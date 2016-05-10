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
	private Long originalTime;
	
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
		originalTime = null;
		if(value != null){
			originalTime = Long.parseLong(value);
			date = new Date(originalTime);
		}
		view.setValue(date);
	}

	@Override
	public String getValue() {
		Date date = view.getValue();
		if(date != null){
			Long time = date.getTime();
			if (originalTime != null) {
				double originalSeconds = Math.floor(originalTime / 1000);
				double newSeconds = Math.floor(time / 1000);
				if (originalSeconds == newSeconds) {
					time = originalTime;
				}
			}
			return Long.toString(time);
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
