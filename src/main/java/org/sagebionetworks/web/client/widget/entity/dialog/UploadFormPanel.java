package org.sagebionetworks.web.client.widget.entity.dialog;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Panel;

public class UploadFormPanel extends FormPanel{
	FlowPanel panel = new FlowPanel();
    FileUpload file = new FileUpload();
    Button submitButton;
    
	public UploadFormPanel(String buttonText) {
		setWidget(panel);
		panel.add(file);
		// Add a 'submit' button.
		submitButton = new Button(buttonText, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		submitButton.addStyleName("margin-top-10");
		panel.add(submitButton);
	}
	
	public Panel getFieldsPanel() {
		return panel;
	}
	public FileUpload getFileUploadField() {
		return file;
	}
	
	public String getFilename() {
		String fullPath = file.getFilename();
		int lastIndex = fullPath.lastIndexOf('\\');
		return fullPath.substring(lastIndex + 1);
	};
	
	public Button getSubmitButton() {
		return submitButton;
	}
}