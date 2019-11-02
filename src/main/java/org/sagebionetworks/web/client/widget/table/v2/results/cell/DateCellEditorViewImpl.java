package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Date;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DateCellEditorViewImpl implements DateCellEditorView {

	public interface Binder extends UiBinder<Widget, DateCellEditorViewImpl> {
	}

	@UiField
	DateTimePicker dateTimePicker;

	Widget widget;

	@Inject
	public DateCellEditorViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setValue(Date value) {
		dateTimePicker.setValue(value);
	}

	@Override
	public Date getValue() {
		return dateTimePicker.getValue();
	}

	@Override
	public int getTabIndex() {
		return dateTimePicker.getTextBox().getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		dateTimePicker.getTextBox().setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		dateTimePicker.getTextBox().setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		dateTimePicker.getTextBox().setTabIndex(index);
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return dateTimePicker.getTextBox().addKeyDownHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		dateTimePicker.getTextBox().fireEvent(event);
	}

}
