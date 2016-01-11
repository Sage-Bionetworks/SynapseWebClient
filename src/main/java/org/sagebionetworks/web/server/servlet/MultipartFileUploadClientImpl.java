package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.file.AddPartResponse;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlRequest;
import org.sagebionetworks.repo.model.file.BatchPresignedUploadUrlResponse;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.file.MultipartUploadStatus;
import org.sagebionetworks.web.client.MultipartFileUploadClient;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class MultipartFileUploadClientImpl extends SynapseClientBase implements
	MultipartFileUploadClient {
	
	@Override
	public MultipartUploadStatus startMultipartUpload(
			MultipartUploadRequest request, Boolean forceRestart)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.startMultipartUpload(request, forceRestart);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public BatchPresignedUploadUrlResponse getMultipartPresignedUrlBatch(
			BatchPresignedUploadUrlRequest request) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.getMultipartPresignedUrlBatch(request);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public AddPartResponse addPartToMultipartUpload(String uploadId,
			int partNumber, String partMD5Hex) throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.addPartToMultipartUpload(uploadId, partNumber, partMD5Hex);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}
	}
	
	@Override
	public MultipartUploadStatus completeMultipartUpload(String uploadId)
			throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
		try {
			return synapseClient.completeMultipartUpload(uploadId);
		} catch (SynapseException e) {
			throw ExceptionUtil.convertSynapseException(e);
		}

	}
}
