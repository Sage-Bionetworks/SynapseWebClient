package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.CheckBox;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileViewOptions implements IsWidget {
	public interface Binder extends UiBinder<Widget, FileViewOptions> {
	}

	Widget widget;
	@UiField
	CheckBox includeFilesCb;
	@UiField
	CheckBox includeFoldersCb;
	@UiField
	CheckBox includeTablesCb;

	@Inject
	public FileViewOptions(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public boolean isIncludeFiles() {
		return includeFilesCb.getValue();
	}

	public void setIsIncludeFiles(boolean value) {
		includeFilesCb.setValue(value);
	}

	public boolean isIncludeFolders() {
		return includeFoldersCb.getValue();
	}

	public void setIsIncludeFolders(boolean value) {
		includeFoldersCb.setValue(value);
	}

	public boolean isIncludeTables() {
		return includeTablesCb.getValue();
	}

	public void setIsIncludeTables(boolean value) {
		includeTablesCb.setValue(value);
	}

	public void addClickHandler(ClickHandler handler) {
		includeFilesCb.addClickHandler(handler);
		includeFoldersCb.addClickHandler(handler);
		includeTablesCb.addClickHandler(handler);
	}
}
