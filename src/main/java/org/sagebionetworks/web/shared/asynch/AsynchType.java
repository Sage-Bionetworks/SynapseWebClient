package org.sagebionetworks.web.shared.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.BulkFileDownloadRequest;
import org.sagebionetworks.repo.model.file.BulkFileDownloadResponse;
import org.sagebionetworks.repo.model.file.S3FileCopyRequest;
import org.sagebionetworks.repo.model.file.S3FileCopyResults;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryNextPageToken;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowReferenceSetResults;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Asynchronous job types.  Maps each type to its corresponding request and response type.
 * @author John
 *
 */
public enum AsynchType implements IsSerializable{
	
	TableAppendRowSet(AppendableRowSetRequest.class, RowReferenceSetResults.class),
	TableQuery(QueryBundleRequest.class, QueryResultBundle.class),
	TableQueryNextPage(QueryNextPageToken.class, QueryResult.class),
	TableCSVUpload(UploadToTableRequest.class, UploadToTableResult.class),
	TableCSVUploadPreview(UploadToTablePreviewRequest.class, UploadToTablePreviewResult.class),
	TableCSVDownload(DownloadFromTableRequest.class, DownloadFromTableResult.class),
	S3FileCopy(S3FileCopyRequest.class, S3FileCopyResults.class),
	BulkFileDownload(BulkFileDownloadRequest.class,BulkFileDownloadResponse.class);
	
	Class<? extends AsynchronousRequestBody> requestClass;
	Class<? extends AsynchronousResponseBody> responseClass;

	private AsynchType(Class<? extends AsynchronousRequestBody> requestClass,
			Class<? extends AsynchronousResponseBody> responseClass) {
		this.requestClass = requestClass;
		this.responseClass = responseClass;
	}

	/**
	 * The class used for this type of request.
	 * @return
	 */
	public Class<? extends AsynchronousRequestBody> getRequestClass() {
		return requestClass;
	}

	/**
	 * The classed used for this type of response.
	 * @return
	 */
	public Class<? extends AsynchronousResponseBody> getResponseClass() {
		return responseClass;
	}
	
}
