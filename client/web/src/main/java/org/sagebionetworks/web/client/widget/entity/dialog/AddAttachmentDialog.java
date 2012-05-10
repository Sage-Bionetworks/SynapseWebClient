package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog.TYPE;


import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;

/**
 * Add an attachment.
 * 
 * @author jmhill
 * 
 */
public class AddAttachmentDialog {
	
	public interface Callback {
		/**
		 * When the user selects save this will be called.
		 * 
		 * @param attachment
		 */
		public void onSaveAttachment(UploadResult result);
	}

	/**
	 * Show the file attachment dialog
	 * 
	 * @param callback
	 */
	public static void showAddAttachmentDialog(String actionUrl, SageImageBundle images, final Callback callback ) {
		// Show a form for adding an Annotations
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(400, 175);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setBlinkModal(true);
		dialog.setButtons(Dialog.CANCEL);
		dialog.setHideOnButtonClick(true);
		dialog.setHeading("Add New File Attachment");
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		
		/**
		 * This window is shown while we wait for the file to upload.
		 */
		final Window loading = DisplayUtils.createLoadingWindow(images, "Uploading...");

		final FormPanel panel = new FormPanel();
		panel.setHeaderVisible(false);
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setShadow(false);
		panel.setAction(actionUrl);
		panel.setEncoding(Encoding.MULTIPART);
		panel.setMethod(Method.POST);
		panel.setButtonAlign(HorizontalAlignment.CENTER);
		panel.setLabelAlign(LabelAlign.RIGHT);
		panel.setBodyBorder(false);

		FileUploadField file = new FileUploadField();
		file.setAllowBlank(false);
		file.setName("uploadedfile");
		file.setFieldLabel("File");
		
		Margins margins = new Margins(10, 10, 0, 10);
		FormData basicFormData = new FormData("-50");
		basicFormData.setMargins(margins);
		
		panel.add(file, basicFormData);

		Button btn = new Button("Attach");
		btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!panel.isValid()) {
					return;
				}
				// normally would submit the form but for example no server set
				// up to
				// handle the post
				panel.submit();
//				dialog.hide();
				loading.show();
			}
		});
	    FormButtonBinding binding = new FormButtonBinding(panel);  
	    binding.addButton(btn);
		// If we do not add this button the panel then it is not part of the form
		panel.addButton(btn);
		// Listen to update events
		panel.addListener(Events.Submit, new Listener<FormEvent>() {

			@Override
			public void handleEvent(FormEvent event) {
				loading.hide();
				dialog.hide();
				UploadResult result = new UploadResult();
				result.setUploadStatus(UploadStatus.SUCCESS);
				if(event != null && event.getResultHtml() != null){
					result = getUploadResult(event.getResultHtml());
				}
				// Let the caller know we are done.
				callback.onSaveAttachment(result);
			}
		});
		dialog.add(panel);
		dialog.show();

	}
	
	private static UploadResult getUploadResult(String html){
		UploadResult result = new UploadResult();
		result.setUploadStatus(UploadStatus.SUCCESS);
		if(html != null){
			GwtAdapterFactory factory = new GwtAdapterFactory();
			String json = html.substring("<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">".length(), (html.length()-"</pre>".length()));
			JSONObjectAdapter adapter;
			try {
				adapter = factory.createNew(json);
				result.initializeFromJSONObject(adapter);
			} catch (JSONObjectAdapterException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
}
