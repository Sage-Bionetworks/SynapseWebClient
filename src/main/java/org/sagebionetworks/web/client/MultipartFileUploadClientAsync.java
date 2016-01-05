package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface MultipartFileUploadClientAsync {

	void startMultipartUpload(MultipartUploadRequest request,
			Boolean forceRestart, AsyncCallback<MultipartUploadStatus> callback);

	void getMultipartPresignedUrlBatch(BatchPresignedUploadUrlRequest request,
			AsyncCallback<BatchPresignedUploadUrlResponse> callback);

	void addPartToMultipartUpload(String uploadId, int partNumber,
			String partMD5Hex, AsyncCallback<AddPartResponse> callback);

	void completeMultipartUpload(String uploadId,
			AsyncCallback<MultipartUploadStatus> callback);

}
