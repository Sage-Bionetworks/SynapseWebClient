package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.ui.HTML;

/**
 * Add an attachment.
 * 
 * @author jmhill
 * 
 */
public class AddAttachmentDialog {
	public static final String ATTACHMENT_FILE_FIELD_ID = "attachmentFileToUpload";
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
	public static void showAddAttachmentDialog(String actionUrl, SageImageBundle images, String windowTitle, String buttonText, final Callback callback ) {
		// Show a form for adding an Annotations
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(400, 175);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setButtons(Dialog.CANCEL);
		dialog.setHideOnButtonClick(true);
		dialog.setHeading(windowTitle);
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		
		dialog.add(getUploadFormPanel(actionUrl, images, buttonText, 75, callback, dialog));
		dialog.show();

	}

	/**
	 * 
	 * @param actionUrl
	 * @param images
	 * @param buttonText
	 * @param callback
	 * @param dialog dialog that will contain this panel (will close automatically if given)
	 * @return
	 */
	public static UploadFormPanel getUploadFormPanel(String actionUrl, SageImageBundle images, String buttonText, int labelWidth, final Callback callback, final Dialog dialog) {
		/**
		 * This window is shown while we wait for the file to upload.
		 */
		final HTML loading = new HTML(DisplayUtils.getLoadingHtml(images, "Uploading..."));
		loading.setVisible(false);
		final FileUploadField file = new FileUploadField();
		
		final UploadFormPanel panel = new UploadFormPanel() {
			
			@Override
			public FileUploadField getFileUploadField() {
				return file;
			}
		};
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
		
		file.setAllowBlank(false);
		file.setName("uploadedfile");
		file.setFieldLabel("File");
		
		final Button btn = new Button(buttonText);
		// Disable until file is selected
		btn.disable();
		btn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (!panel.isValid()) {
					return;
				}
				// normally would submit the form but for example no server set
				// up to
				// handle the post
				btn.disable();
				panel.submit();
				loading.setVisible(true);
			}
		});
		
		file.addListener(Events.OnChange, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				final String fullPath = file.getValue();
				final int lastIndex = fullPath.lastIndexOf('\\');
				final String fileName = fullPath.substring(lastIndex + 1);
				file.getFileInput().setId(ATTACHMENT_FILE_FIELD_ID);
				file.setValue(fileName);
				// Now enable for submission
				btn.enable();
			}
		});
		file.setWidth(365);
		
		FormData basicFormData = new FormData("-50");
		Margins margins = new Margins(10, 10, 0, 10);
		basicFormData.setMargins(margins);
		
		panel.add(file, basicFormData);
		panel.add(loading, basicFormData);
		// If we do not add this button the panel then it is not part of the form
		panel.addButton(btn);
		// Listen to update events
		panel.addListener(Events.Submit, new Listener<FormEvent>() {

			@Override
			public void handleEvent(FormEvent event) {
				btn.enable();
				loading.setVisible(false);
				if (dialog != null)
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
		
		panel.setLabelWidth(labelWidth);
		return panel;
	}
	
	public static UploadResult getUploadResult(String html){
		UploadResult result = new UploadResult();
		result.setUploadStatus(UploadStatus.SUCCESS);
		if(html != null){
			GwtAdapterFactory factory = new GwtAdapterFactory();
			//search for the first ">" (end of the pre tag)
			int closeIndex = html.indexOf(">")+1;
			String json = html;
			if (html.contains("</pre>"))
				json = html.substring(closeIndex, (html.length()-"</pre>".length()));
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
