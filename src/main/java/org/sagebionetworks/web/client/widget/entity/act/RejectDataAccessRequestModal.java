package org.sagebionetworks.web.client.widget.entity.act;

import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WebConstants;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class RejectDataAccessRequestModal implements RejectDataAccessRequestModalView.Presenter, IsWidget {

	// Template rejection format
	public static String TEMPLATE_HEADER_THANKS = "Thank you for submitting your data access request. Before I can accept your request, please address the following:\n";
	public static String TEMPLATE_HEADER_SIGNATURE = "\nPlease contact us at act@sagebionetworks.org if you have any questions.\n" + "\n" + "Regards,\n" + "Access and Compliance Team (ACT)\n" + "act@sagebionetworks.org";

	// If no options are shown for rejected reason
	public static String ERROR_MESSAGE = "Error: Please select at least one checkbox and generate a response or manually enter a response";

	private RejectDataAccessRequestModalView view;
	CallbackP<String> callback;
	private SynapseProperties synapseProperties;
	private RequestBuilderWrapper requestBuilder;
	private JSONObjectAdapter jsonObjectAdapter;
	private PresignedURLAsyncHandler presignedUrlAsyncHandler;
	private SynapseJavascriptClient jsClient;
	private boolean isLoaded = false;

	@Inject
	public RejectDataAccessRequestModal(RejectDataAccessRequestModalView view, SynapseProperties synapseProperties, RequestBuilderWrapper requestBuilder, JSONObjectAdapter jsonObjectAdapter, PresignedURLAsyncHandler presignedUrlAsyncHandler, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.synapseProperties = synapseProperties;
		this.requestBuilder = requestBuilder;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.presignedUrlAsyncHandler = presignedUrlAsyncHandler;
		this.jsClient = jsClient;
		this.view.setPresenter(this);
	}

	public void show(CallbackP<String> callback) {
		this.view.clear();
		if (!isLoaded) {
			// get the json file that define the reasons
			String jsonSynID = synapseProperties.getSynapseProperty(WebConstants.ACT_DATA_ACCESS_REJECTION_REASONS_PROPERTY_KEY);
			this.callback = callback;
			// get the entity (to find it's file handle ID)
			jsClient.getEntity(jsonSynID, new AsyncCallback<Entity>() {
				@Override
				public void onSuccess(Entity result) {
					getPresignedURLForReasons(jsonSynID, ((FileEntity) result).getDataFileHandleId());				
				}
	
				@Override
				public void onFailure(Throwable caught) {
					view.showError(caught.getMessage());
				}
			});
		} else {
			view.show();
		}
	}

	public void getPresignedURLForReasons(String synID, String fileHandleID) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setAssociateObjectId(synID);
		fha.setFileHandleId(fileHandleID);
		presignedUrlAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onSuccess(FileResult result) {
				getReasons(result.getPreSignedURL());
			};
			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
			}
		});
	}


	public void getReasons(String url) {
		view.clearReasons();
		requestBuilder.configure(RequestBuilder.GET, url);
		requestBuilder.setHeader(WebConstants.CONTENT_TYPE, WebConstants.TEXT_PLAIN_CHARSET_UTF8);
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						String text = response.getText();
						try {
							// parse json, and add each reason
							JSONObjectAdapter json = jsonObjectAdapter.createNew(text);
							JSONArrayAdapter jsonArray = json.getJSONArray("reasons");
							for (int i = 0; i < jsonArray.length(); i++) {
								view.addReason(jsonArray.getString(i));
							}
							view.show();
							isLoaded = true;
						} catch (JSONObjectAdapterException e) {
							onError(null, e);
						}						
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve message ACT rejection reasons. Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					view.showError(exception.getMessage());					
				}
			});
		} catch (final Exception e) {
			view.showError(e.getMessage());
		}
	}
	@Override
	public void updateResponse() {
		String output = view.getSelectedCheckboxText();
		view.setValue(TEMPLATE_HEADER_THANKS + output + TEMPLATE_HEADER_SIGNATURE);
	}

	@Override
	public void onSave() {
		if (view.getValue().equals("")) {
			view.showError(ERROR_MESSAGE);
		} else {
			callback.invoke(view.getValue());
			view.hide();
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	};
}
