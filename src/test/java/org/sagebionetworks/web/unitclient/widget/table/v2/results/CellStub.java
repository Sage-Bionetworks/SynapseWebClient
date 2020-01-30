package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple cell stub.
 * 
 * @author John
 *
 */
public class CellStub implements CellEditor {

	private String value;
	boolean isValid = true;

	@Override
	public Widget asWidget() {
		return null;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}


	@Override
	public int getTabIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAccessKey(char key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus(boolean focused) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTabIndex(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// TODO Auto-generated method stub

	}

	public void setIsValid(boolean isValid) {
		this.isValid = isValid;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

}
