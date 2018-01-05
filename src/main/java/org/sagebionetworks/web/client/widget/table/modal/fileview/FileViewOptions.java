package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.InlineCheckBox;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileViewOptions implements IsWidget {
	public interface Binder extends UiBinder<Widget, FileViewOptions> {}
	Widget widget;
	@UiField
	InlineCheckBox includeTablesCb;
	
	@Inject
	public FileViewOptions(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	public boolean isIncludeTables() {
		return includeTablesCb.getValue();
	}

	public void setIsIncludeTables(boolean value) {
		includeTablesCb.setValue(value);
	}
	
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return includeTablesCb.addClickHandler(handler);
	}
}
