package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTrackerImpl;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.ResultNotReadyException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Complex test for the AsynchronousJobTracker
 * 
 * @author John
 *
 */
public class AsynchronousJobTrackerTest {
	@Mock
	SynapseJavascriptClient mockJsClient;
	TimerProviderStub mockTimerProvider;
	AdapterFactory adapterFactory;
	int waitTimeMS;
	UpdatingAsynchProgressHandler mockHandler;
	AsynchronousJobTrackerImpl tracker;
	AsynchronousJobStatus start;
	ResultNotReadyException startNotReady;
	AsynchronousJobStatus middle;
	ResultNotReadyException middleNotReady;
	AsynchronousJobStatus done;
	ResultNotReadyException doneNotReady;
	DownloadFromTableRequest requestBody;
	DownloadFromTableResult responseBody;
	AsynchType type;
	String jobId;
	String tableId;

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockTimerProvider = new TimerProviderStub();
		adapterFactory = new AdapterFactoryImpl();
		waitTimeMS = 1000;
		mockHandler = Mockito.mock(UpdatingAsynchProgressHandler.class);
		tracker = new AsynchronousJobTrackerImpl(mockTimerProvider, mockJsClient);

		// Setup three phases for a job.
		jobId = "99999";
		tableId = "syn123";
		// This job will have three phases.
		start = new AsynchronousJobStatus();
		start.setJobId(jobId);
		start.setJobState(AsynchJobState.PROCESSING);
		start.setProgressCurrent(0l);
		start.setProgressTotal(100l);
		startNotReady = new ResultNotReadyException(start);
		// In the middle
		middle = new AsynchronousJobStatus();
		middle.setJobId(jobId);
		middle.setJobState(AsynchJobState.PROCESSING);
		middle.setProgressCurrent(50l);
		middle.setProgressTotal(100l);
		middleNotReady = new ResultNotReadyException(middle);
		// done
		done = new AsynchronousJobStatus();
		done.setJobId(jobId);
		done.setJobState(AsynchJobState.COMPLETE);
		done.setProgressCurrent(100l);
		done.setProgressTotal(100l);
		doneNotReady = new ResultNotReadyException(done);
		requestBody = new DownloadFromTableRequest();
		requestBody.setSql("select * from " + tableId);
		requestBody.setEntityId(tableId);
		type = AsynchType.TableCSVDownload;

		responseBody = new DownloadFromTableResult();
		responseBody.setEtag("etag123");
		responseBody.setTableId(tableId);

		when(mockHandler.isAttached()).thenReturn(true);
	}

	@Test
	public void testMultipleStatesThenSuccess() throws JSONObjectAdapterException {
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockJsClient).startAsynchJob(any(AsynchType.class), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		// simulate three calls
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, responseBody).when(mockJsClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		// Update should occur for all three phases
		verify(mockHandler).onUpdate(start);
		verify(mockHandler).onUpdate(middle);
		verify(mockHandler).onUpdate(done);
		// It should also be updated when done
		verify(mockHandler).onComplete(responseBody);
		verify(mockHandler, never()).onCancel();
		verify(mockHandler, never()).onFailure(any(Throwable.class));
	}

	@Test
	public void testWithFailure() throws JSONObjectAdapterException {
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockJsClient).startAsynchJob(any(AsynchType.class), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		Throwable error = new Throwable("Something went wrong");
		AsyncMockStubber.callFailureWith(error).when(mockJsClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		// It should also be updated when done
		verify(mockHandler, never()).onComplete(any(AsynchronousResponseBody.class));
		verify(mockHandler, never()).onCancel();
		// The error must be passed to the handler
		verify(mockHandler).onFailure(error);
	}

	@Test
	public void testMultipleStatesThenFailure() throws JSONObjectAdapterException {
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockJsClient).startAsynchJob(any(AsynchType.class), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		// simulate three calls
		Throwable exception = new Throwable("Something went wrong");
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, exception).when(mockJsClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		// Update should occur for all three phases
		verify(mockHandler).onUpdate(start);
		verify(mockHandler).onUpdate(middle);
		verify(mockHandler).onUpdate(done);
		// It should also be updated when done
		verify(mockHandler).onFailure(exception);
		verify(mockHandler, never()).onCancel();
		verify(mockHandler, never()).onComplete(any(AsynchronousResponseBody.class));
	}

	@Test
	public void testCancel() throws JSONObjectAdapterException {
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockJsClient).startAsynchJob(any(AsynchType.class), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		// These will still be called ever after the cancel.
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, responseBody).when(mockJsClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		// Since this test is not using a multiple threads cancel must be called before we start.
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		tracker.cancel();
		assertTrue(this.mockTimerProvider.isCancled());
		// Since cancel happens after complete, complete should still be called.
		verify(mockHandler).onComplete(any(AsynchronousResponseBody.class));
		// The handler should not get the onCancle() because the onComplete() would have already been sent.
		verify(mockHandler, never()).onCancel();
	}

	/**
	 * This is a test for SWC-1780.
	 */
	@Test
	public void testDetached() {
		// Setup a case were the handler starts attached but then becomes detached.
		when(mockHandler.isAttached()).thenReturn(true, true, false);
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockJsClient).startAsynchJob(any(AsynchType.class), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		// simulate three calls
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, responseBody).when(mockJsClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsynchronousRequestBody.class), any(AsyncCallback.class));
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		// Update should occur for all three phases
		verify(mockHandler).onUpdate(start);
		verify(mockHandler).onUpdate(middle);
		verify(mockHandler).onUpdate(done);
		// detachment should be slient.
		verify(mockHandler, never()).onComplete(any(AsynchronousResponseBody.class));
		verify(mockHandler, never()).onCancel();
		verify(mockHandler, never()).onFailure(any(Throwable.class));
	}
}
