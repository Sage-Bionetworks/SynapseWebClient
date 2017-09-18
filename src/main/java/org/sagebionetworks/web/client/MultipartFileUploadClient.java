
package org.sagebionetworks.web.client;

import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("multipartFileUploadClient")	
public interface MultipartFileUploadClient extends RemoteService {

	MultipartUploadStatus startMultipartUpload(MultipartUploadRequest request, Boolean forceRestart) throws RestServiceException;
	BatchPresignedUploadUrlResponse getMultipartPresignedUrlBatch(BatchPresignedUploadUrlRequest request) throws RestServiceException;
	AddPartResponse addPartToMultipartUpload(String uploadId, int partNumber, String partMD5Hex) throws RestServiceException;
	MultipartUploadStatus completeMultipartUpload(String uploadId) throws RestServiceException;
}
