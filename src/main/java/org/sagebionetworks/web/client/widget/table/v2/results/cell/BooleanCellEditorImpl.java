package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.web.client.StringUtils;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
/**
 * The boolean cell editor allows for nothing, true, and false.
 * @author John
 *
 */
public class BooleanCellEditorImpl implements BooleanCellEditor {
	
	public static final String NOTHING_SELECTED = "nothing selected";
	public static final String FALSE = "false";
	public static final String TRUE = "true";

	/**
	 * The possible values for this editor.
	 */
	private static final List<String> VALUES = Arrays.asList(NOTHING_SELECTED, TRUE, FALSE);
		
	ListCellEdtiorView view;
	
	@Inject
	public BooleanCellEditorImpl(ListCellEdtiorView view) {
		this.view = view;
		view.configure(VALUES);
	}

	@Override
	public boolean isValid() {
		// all selections are valid
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		value = StringUtils.trimWithEmptyAsNull(value);
		if(value == null){
			view.setValue(0);
		}else{
			if(Boolean.parseBoolean(value)){
				view.setValue(1);
			}else{
				view.setValue(2);
			}
		}
	}

	@Override
	public String getValue() {
		switch (view.getValue()){
		case 0:
			return null;
		case 1:
			return TRUE;
		default:
			return FALSE;
		}
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		// This ListBox handles key events so we do not wan to forward them.
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// This ListBox handles key events so we do not wan to forward them.
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
