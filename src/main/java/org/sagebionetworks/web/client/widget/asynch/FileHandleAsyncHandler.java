package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileHandleAsyncHandler {
	void getFileHandle(FileHandleAssociation entityId, AsyncCallback<FileResult> callback);
}
