package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;

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
	 * 
	 * @param actionUrl
	 * @param images
	 * @param buttonText
	 * @param callback
	 * @return
	 */
	public static UploadFormPanel getUploadFormPanel(String actionUrl, String buttonText, final Callback callback) {
		final UploadFormPanel form = new UploadFormPanel(buttonText);
		
		form.setAction(actionUrl);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
	    form.setMethod(FormPanel.METHOD_POST);
	    form.getFileUploadField().setName("uploadedfile");
		
		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				UploadResult result = new UploadResult();
				result.setUploadStatus(UploadStatus.SUCCESS);
				if(event != null && event.getResults() != null){
					result = getUploadResult(event.getResults());
				}
				// Let the caller know we are done.
				callback.onSaveAttachment(result);
			}
		});
	    
	    return form;
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
