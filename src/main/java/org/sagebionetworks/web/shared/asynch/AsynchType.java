package org.sagebionetworks.web.shared.asynch;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.*;
import org.sagebionetworks.repo.model.doi.v2.*;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResponse;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListResponse;
import org.sagebionetworks.repo.model.file.BulkFileDownloadRequest;
import org.sagebionetworks.repo.model.file.BulkFileDownloadResponse;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.HasEntityId;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryNextPageToken;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowReferenceSetResults;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
import org.sagebionetworks.repo.model.file.*;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Asynchronous job types.  Maps each type to its corresponding request and response type.
 * @author John
 *
 */

public enum AsynchType implements IsSerializable{
	TableAppendRowSet(TABLE_APPEND, AppendableRowSetRequest.class, RowReferenceSetResults.class),
	TableQuery(TABLE_QUERY, QueryBundleRequest.class, QueryResultBundle.class),
	TableQueryNextPage(TABLE_QUERY_NEXTPAGE, QueryNextPageToken.class, QueryResult.class),
	TableCSVUpload(TABLE_UPLOAD_CSV, UploadToTableRequest.class, UploadToTableResult.class),
	TableCSVUploadPreview(TABLE_UPLOAD_CSV_PREVIEW, UploadToTablePreviewRequest.class, UploadToTablePreviewResult.class),
	TableCSVDownload(TABLE_DOWNLOAD_CSV, DownloadFromTableRequest.class, DownloadFromTableResult.class),
	BulkFileDownload(FILE_BULK, BulkFileDownloadRequest.class,BulkFileDownloadResponse.class),
	TableTransaction(TABLE_TRANSACTION, TableUpdateTransactionRequest.class, TableUpdateTransactionResponse.class),
	Doi(DOI, DoiRequest.class, DoiResponse.class),
	AddFileToDownloadList(DOWNLOAD_LIST_ADD, AddFileToDownloadListRequest.class, AddFileToDownloadListResponse.class);
	String prefix;
	Class<? extends AsynchronousRequestBody> requestClass;
	Class<? extends AsynchronousResponseBody> responseClass;

	private AsynchType(String prefix, 
			Class<? extends AsynchronousRequestBody> requestClass,
			Class<? extends AsynchronousResponseBody> responseClass) {
		this.prefix = prefix;
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
	
	public String getPrefix() {
		return prefix;
	}
	
	/*
	 * extracts the entityId from the request body 
	 * throws an exception if the request has an entityId field but the entityId is null
	 * If the request body does not have an entityId field, returns null.
	 */
	private String getEntityIdFromRequest(AsynchronousRequestBody request) {
		if (request instanceof UploadToTableRequest && ((UploadToTableRequest) request).getTableId() != null) {
			return ((UploadToTableRequest) request).getTableId();
		} else if (request instanceof HasEntityId && ((HasEntityId) request).getEntityId() != null) {
			return ((HasEntityId) request).getEntityId();
		} else if ((request instanceof UploadToTableRequest && ((UploadToTableRequest) request).getTableId() == null) ||
					(request instanceof HasEntityId && ((HasEntityId) request).getEntityId() == null)) {
			throw new IllegalArgumentException("entityId cannot be null");
		} else {
			return null;
		}
	}
	
	/**
	 * Get the URL used to get the results for this job type.
	 * @param token
	 * @param request
	 * @return
	 */
	public String getResultUrl(String token, AsynchronousRequestBody request){
		return getResultUrl(token, getEntityIdFromRequest(request));	
	}

	public String getResultUrl(String token, String entityId){
		if (entityId != null) {
			return "/entity/" + entityId + prefix + ASYNC_GET + token;
		}
		return prefix+ASYNC_GET + token;
	}
	
	public String getStartUrl(AsynchronousRequestBody request){
		String entityId = getEntityIdFromRequest(request);
		if (entityId != null) {
			return "/entity/" + entityId + prefix + ASYNC_START;
		} else {
			return prefix + ASYNC_START;
		}
	}
}
