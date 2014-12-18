package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellEditorImpl implements FileCellEditor, FileCellEditorView.Presenter{
	
	FileCellEditorView view;
	
	@Inject
	FileCellEditorImpl(FileCellEditorView view){
		this.view = view;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setValue(String value) {
		this.view.setValue(value);
	}

	@Override
	public String getValue() {
		return this.view.getValue();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		// cannot handle for this widget
		return null;
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// cannot handle for this widget
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
	public void onUpload() {
		view.showModal();
		
	}

}
