package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.gwt.client.schema.adapter.GwtAdapterFactory;
import org.sagebionetworks.repo.model.attachment.UploadResult;
import org.sagebionetworks.repo.model.attachment.UploadStatus;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Add an attachment.
 * 
 * @author jmhill
 * 
 */
public class AddAttachmentHelper {
	public static UploadResult getUploadResult(String html) {
		UploadResult result = new UploadResult();
		result.setUploadStatus(UploadStatus.SUCCESS);
		if (html != null) {
			GwtAdapterFactory factory = new GwtAdapterFactory();
			// search for the first ">" (end of the pre tag)
			int closeIndex = html.indexOf(">") + 1;
			String json = html;
			if (html.contains("</pre>"))
				json = html.substring(closeIndex, (html.length() - "</pre>".length()));
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
