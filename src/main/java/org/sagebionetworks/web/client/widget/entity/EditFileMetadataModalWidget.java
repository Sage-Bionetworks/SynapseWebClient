package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface EditFileMetadataModalWidget extends IsWidget {
	/**
	 * @param fileEntity File entity to edit.
	 * @param fileHandle The file handle.
	 * @param handler callback after the entity has been successfully updated.
	 */
	public void configure(FileEntity fileEntity, FileHandle fileHandle, Callback handler);
}
