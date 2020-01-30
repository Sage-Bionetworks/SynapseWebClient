package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface PresignedAndFileHandleURLAsyncHandler {
	void getFileResult(FileHandleAssociation fileHandleAssociation, AsyncCallback<FileResult> callback);
}
