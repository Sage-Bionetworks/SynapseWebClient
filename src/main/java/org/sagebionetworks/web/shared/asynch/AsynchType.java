package org.sagebionetworks.web.shared.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.AppendableRowSetRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.RowReferenceSetResults;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Asynchronous job types.  Maps each type to its corresponding request and response type.
 * @author John
 *
 */
public enum AsynchType implements IsSerializable{
	
	TableAppendRowSet(AppendableRowSetRequest.class, RowReferenceSetResults.class),
	TableQuery(QueryBundleRequest.class, QueryResultBundle.class),
	TableCSVUploadPreview(UploadToTablePreviewRequest.class, UploadToTablePreviewResult.class),
	TableCSVDownload(DownloadFromTableRequest.class, DownloadFromTableResult.class),
	TableTransaction(TableUpdateTransactionRequest.class, TableUpdateTransactionResponse.class);
	
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
