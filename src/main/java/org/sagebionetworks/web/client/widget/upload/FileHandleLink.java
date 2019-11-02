package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget representing a file handle, on click will call back with the file handle id. If
 * isSelectable then will show a checkbox.
 * 
 * @author jayhodgson
 *
 */
public class FileHandleLink implements IsWidget, SelectableListItem {
	public interface FileHandleLinkUiBinder extends UiBinder<Widget, FileHandleLink> {
	}

	@UiField
	CheckBox select;
	@UiField
	Span fileHandleWidgetContainer;

	Widget widget;

	FileHandleWidget fileHandleWidget;
	Callback selectionChangedCallback;

	@Inject
	public FileHandleLink(FileHandleLinkUiBinder binder, FileHandleWidget fileHandleWidget) {
		widget = binder.createAndBindUi(this);
		this.fileHandleWidget = fileHandleWidget;
		fileHandleWidgetContainer.add(fileHandleWidget);
		select.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (selectionChangedCallback != null) {
					selectionChangedCallback.invoke();
				}
			}
		});
	}

	public FileHandleLink configure(String fileName, String rawFileHandleId) {
		fileHandleWidget.configure(fileName, rawFileHandleId);
		return this;
	}

	public FileHandleLink configure(FileHandleAssociation fha) {
		fileHandleWidget.configure(fha);
		return this;
	}

	public FileHandleLink setFileSelectCallback(Callback selectionChangedCallback) {
		this.selectionChangedCallback = selectionChangedCallback;
		return this;
	}

	public boolean isSelected() {
		return select.getValue();
	}

	public void setSelected(boolean selected) {
		select.setValue(selected, true);
	}

	public void setSelectVisible(boolean visible) {
		select.setVisible(visible);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public String getFileHandleId() {
		return fileHandleWidget.getFileHandleId();
	}
}
