package org.sagebionetworks.web.client.widget;

import java.util.Date;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.PresignedAndFileHandleURLAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DownloadSpeedTesterImpl implements DownloadSpeedTester {
	public static final String TEST_FILE_SYN_ID = "syn12600511";
	long startTime;
	RequestBuilderWrapper requestBuilder;
	PresignedAndFileHandleURLAsyncHandler presignedUrlAsyncHandler;
	
	AuthenticationController authController;
	SynapseJavascriptClient jsClient;
	/**
	 * Three step process.
	 * 1.  Get the test file entity (for the target file handle id).
	 * 2.  Get the presigned url and file size.
	 * 3.  Run the test - how much time does it take to download the target file?
	 * 
	 * If we want to make the test more accurate, we could increase the size of the data file.
	 * Note: Make sure the data file is properly optimized and compressed. The default compression on connections to the webserver would cause an overestimate if this is not the case.
	 * Not sure why every app needs to run their own speed test. NetInfo is not well-supported at the time of writing: https://caniuse.com/#feat=netinfo
	 * @param authController
	 * @param presignedUrlAsyncHandler
	 * @param requestBuilder
	 * @param jsClient
	 */
	@Inject
	public DownloadSpeedTesterImpl(
			AuthenticationController authController,
			PresignedAndFileHandleURLAsyncHandler presignedUrlAsyncHandler,
			RequestBuilderWrapper requestBuilder,
			SynapseJavascriptClient jsClient) {
		this.authController = authController;
		this.presignedUrlAsyncHandler = presignedUrlAsyncHandler;
		this.requestBuilder = requestBuilder;
		this.jsClient = jsClient;
	}
	@Override
	public void testDownloadSpeed(final AsyncCallback<Double> callback) {
		// must be logged in to check download speed
		if (authController.isLoggedIn()) {
			jsClient.getEntity(TEST_FILE_SYN_ID, new AsyncCallback<Entity>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				public void onSuccess(Entity entity) {
					FileEntity file = (FileEntity)entity;
					FileHandleAssociation fha = new FileHandleAssociation();
					fha.setAssociateObjectId(TEST_FILE_SYN_ID);
					fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
					fha.setFileHandleId(file.getDataFileHandleId());
					testDownloadSpeedStep2(fha, callback);
				};
			});
		} else {
			callback.onFailure(new UnauthorizedException("Must be logged in to check download speed."));
		}
	}
	
	public void testDownloadSpeedStep2(FileHandleAssociation fha, AsyncCallback<Double> callback) {
		presignedUrlAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			public void onSuccess(FileResult fileResult) {
				if (fileResult.getFailureCode()  == null) {
					double fileSize = fileResult.getFileHandle().getContentSize().doubleValue();
					String url = fileResult.getPreSignedURL();
					testDownloadSpeedStep3(url, fileSize, callback);	
				} else {
					onFailure(new Exception("Unable to get the test file presigned url and file handle: " + fileResult.getFailureCode().toString()));
				}
			};
		});
	}
	
	public void testDownloadSpeedStep3(String url, double fileSize, AsyncCallback<Double> callback) {
		requestBuilder.configure(RequestBuilder.GET, url);
		try {
			startTime = new Date().getTime();
			requestBuilder.sendRequest(null, new RequestCallback() {

				@Override
				public void onResponseReceived(Request request,
						Response response) {
					int statusCode = response.getStatusCode();
					if (statusCode == Response.SC_OK) {
						// done!
						long endTime = new Date().getTime();
						long msElapsed = endTime - startTime;
						callback.onSuccess(fileSize / (msElapsed / 1000));
					} else {
						onError(null, new IllegalArgumentException("Unable to retrieve test file. Reason: " + response.getStatusText()));
					}
				}

				@Override
				public void onError(Request request, Throwable exception) {
					callback.onFailure(exception);
				}
			});
		} catch (final Exception e) {
			callback.onFailure(e);
		}
	}
}
