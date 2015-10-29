package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface EditFileMetadataModalWidget extends IsWidget{
	/**
	 * @param fileEntity File entity to edit.
	 * @param fileName The file name (that the presigned URL would cause the client) to use during download.
	 * @param handler callback after the entity has been successfully updated.
	 */
	public void configure(FileEntity fileEntity, String fileName, Callback handler);
}
