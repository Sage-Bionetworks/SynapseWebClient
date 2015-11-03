package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Widget representing a file handle, on click will call back with the file handle id.
 * If isSelectable then will show a checkbox.
 * @author jayhodgson
 *
 */
public class FileHandleLink implements IsWidget {
	public interface FileHandleLinkUiBinder extends UiBinder<Widget, FileHandleLink> {}
	
	@UiField
	CheckBox select;
	@UiField
	Anchor fileHandleLink;
	
	Widget widget;
	
	String fileHandleId;
	CallbackP<String> fileClickedCallback;
	@Inject
	public FileHandleLink(FileHandleLinkUiBinder binder) {
		widget = binder.createAndBindUi(this);
		
		fileHandleLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fileClickedCallback.invoke(fileHandleId);
			}
		});
	}
	
	public void configure(String fileHandleId, String fileName, CallbackP<String> fileClickedCallback) {
		this.fileClickedCallback = fileClickedCallback;
		this.fileHandleId = fileHandleId;
		fileHandleLink.setText(fileName);
	}
	
	public boolean isSelected() {
		return select.getValue();
	}
	public void setSelected(boolean selected){
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
		return fileHandleId;
	}
}
