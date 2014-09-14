package org.sagebionetworks.web.shared.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryNextPageToken;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;





import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Asynchronous job types.  Maps each type to its corresponding request and response type.
 * @author John
 *
 */
public enum AsynchType implements IsSerializable{
	
	TableQuery(QueryBundleRequest.class, QueryResultBundle.class),
	TableQueryNextPage(QueryNextPageToken.class, QueryResult.class),
	TableCSVUpload(UploadToTableRequest.class, UploadToTableResult.class),
	TableCSVDownload(DownloadFromTableRequest.class, DownloadFromTableResult.class);
	
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
