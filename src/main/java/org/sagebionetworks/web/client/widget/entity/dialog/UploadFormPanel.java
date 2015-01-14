package org.sagebionetworks.web.client.widget.entity.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.DisplayUtils;

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
    
	public UploadFormPanel(final String buttonText) {
		setWidget(panel);
		panel.add(file);
		// Add a 'submit' button.
		submitButton = new Button(buttonText, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isDefined(file.getFilename())) {
					DisplayUtils.showErrorMessage("Please select a file");
				} else {
					submitButton.setText("Uploading...");
					submitButton.setEnabled(false);
					submit();	
				}
			}
		});
		submitButton.addStyleName("margin-top-10");
		panel.add(submitButton);
		
		
		addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				submitButton.setText(buttonText);
				submitButton.setEnabled(true);
			}
		});
	}
	
	public Panel getFieldsPanel() {
		return panel;
	}
	
	public FileUpload getFileUploadField() {
		return file;
	}
	
	public void setAccept(String acceptedMimeTypes) {
		file.getElement().setAttribute("accept", acceptedMimeTypes);
	}
	
	public String getFilename() {
		String fullPath = file.getFilename();
		int lastIndex = fullPath.lastIndexOf('\\');
		return fullPath.substring(lastIndex + 1);
	};
}