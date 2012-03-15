package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.event.DatePickerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.DatePicker;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Menu;

public class ListMenu extends Menu {

	/**
	 * The internal date picker.
	 */
	protected ListEditorGrid<String> picker;

	public ListMenu(IconsImageBundle iconBundle, ClientLogger logger) {
		picker = new ListEditorGrid<String>(iconBundle, logger);
		picker.addListener(Events.Select, new Listener<DatePickerEvent>() {
			public void handleEvent(DatePickerEvent be) {
				onPickerSelect(be);
			}
		});
		add(picker);
		addStyleName("x-date-menu");
		setAutoHeight(true);
		plain = true;
		showSeparator = false;
		setEnableScrolling(false);
	}

	@Override
	public void focus() {
		super.focus();
		picker.el().focus();
	}

	/**
	 * Returns the selected date.
	 * 
	 * @return the date
	 */
	public List<String> getList() {
		return picker.getList();
	}

	/**
	 * Returns the date picker.
	 * 
	 * @return the date picker
	 */
	public ListEditorGrid<String> getListPicker() {
		return picker;
	}

	/**
	 * Sets the menu's date.
	 * 
	 * @param date
	 *            the date
	 */
	public void setList(List<String> list) {
		picker.setList(list, new TextField<String>());
	}

	protected void onPickerSelect(DatePickerEvent be) {
		MenuEvent e = new MenuEvent(this);
		e.setDate(be.getDate());
		fireEvent(Events.Select, e);
	}

}
