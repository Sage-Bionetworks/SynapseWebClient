package org.sagebionetworks.web.client.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This runner tracks an asynchronous table query.
 * @author John
 *
 */
public class AsynchTableQueryRunner implements AsynchJobRunner {
	
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	
	@Inject
	public AsynchTableQueryRunner(SynapseClientAsync synapseClient, AdapterFactory adapterFactory){
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
	}

	@Override
	public void startJob(AsynchronousRequestBody request,
			AsyncCallback<String> callback) {
		try {
			// Convert to json
			String json = request.writeToJSONObject(this.adapterFactory.createNew()).toJSONString();
			synapseClient.startTableQueryAsynchJob(json, callback);
		} catch (JSONObjectAdapterException e) {
			callback.onFailure(e);
		}		
	}

	@Override
	public void getJob(String jobId, final AsyncCallback<AsynchronousResponseBody> callback) {
		synapseClient.getTableQueryAsynchJob(jobId, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(String resultJOSN) {
				try {
					QueryResultBundle bundle = new QueryResultBundle(adapterFactory.createNew(resultJOSN));
					callback.onSuccess(bundle);
				} catch (JSONObjectAdapterException e) {
					callback.onFailure(e);
				}
			}
		});
		
	}


}
