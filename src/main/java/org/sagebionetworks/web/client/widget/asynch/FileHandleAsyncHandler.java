package org.sagebionetworks.web.client.widget.asynch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;

public interface FileHandleAsyncHandler {
  void getFileResult(
    FileHandleAssociation fileHandleAssociation,
    AsyncCallback<FileResult> callback
  );
}
