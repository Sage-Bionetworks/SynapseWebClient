package org.sagebionetworks.web.client.widget;

import java.util.Date;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DownloadSpeedTesterImpl implements DownloadSpeedTester {
	public static final double FILE_SIZE_BYTES = 5083219;
	long startTime;
	RequestBuilderWrapper requestBuilder;
	PresignedURLAsyncHandler presignedUrlAsyncHandler;
	FileHandleAssociation fha;
	AuthenticationController authController;
	@Inject
	public DownloadSpeedTesterImpl(
			AuthenticationController authController,
			PresignedURLAsyncHandler presignedUrlAsyncHandler,
			RequestBuilderWrapper requestBuilder) {
		this.authController = authController;
		this.presignedUrlAsyncHandler = presignedUrlAsyncHandler;
		this.requestBuilder = requestBuilder;
		fha = new FileHandleAssociation();
		fha.setAssociateObjectId("syn12600511");
		fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
		fha.setFileHandleId("27936174");
	}
	
	@Override
	public void testDownloadSpeed(final AsyncCallback<Double> callback) {
		// must be logged in to check download speed
		if (authController.isLoggedIn()) {
			presignedUrlAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
				public void onSuccess(FileResult fileResult) {
					runTest(fileResult.getPreSignedURL(), callback);
				};
			});
		} else {
			callback.onFailure(new UnauthorizedException("Must be logged in to check download speed."));
		}
	}
	
	public void runTest(String url, AsyncCallback<Double> callback) {
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
						callback.onSuccess(FILE_SIZE_BYTES / (msElapsed / 1000));
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
