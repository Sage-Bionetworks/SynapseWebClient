package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;
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

	SynapseClientAsync mockSynapseClient;
	TimerProviderStub mockTimerProvider;
	AdapterFactory adapterFactory;
	int waitTimeMS;
	UpdatingAsynchProgressHandler mockHandler;
	AsynchronousJobTrackerImpl tracker;
	AsynchronousJobStatus start;
	String startJSON;
	ResultNotReadyException startNotReady;
	AsynchronousJobStatus middle;
	String middleJSON;
	ResultNotReadyException middleNotReady;
	AsynchronousJobStatus done;
	String doneJSON;
	ResultNotReadyException doneNotReady;
	DownloadFromTableRequest requestBody;
	DownloadFromTableResult responseBody;
	String responseJSON;
	AsynchType type;
	String jobId;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockTimerProvider = new TimerProviderStub();
		adapterFactory = new AdapterFactoryImpl();
		waitTimeMS = 1000;
		mockHandler = Mockito.mock(UpdatingAsynchProgressHandler.class);
		tracker = new AsynchronousJobTrackerImpl(mockSynapseClient, mockTimerProvider, adapterFactory);
		
		// Setup three phases for a job.
		jobId = "99999";
		// This job will have three phases.
		start = new AsynchronousJobStatus();
		start.setJobId(jobId);
		start.setJobState(AsynchJobState.PROCESSING);
		start.setProgressCurrent(0l);
		start.setProgressTotal(100l);
		startJSON = EntityFactory.createJSONStringForEntity(start);
		startNotReady = new ResultNotReadyException(startJSON);
		// In the middle
		middle = new AsynchronousJobStatus();
		middle.setJobId(jobId);
		middle.setJobState(AsynchJobState.PROCESSING);
		middle.setProgressCurrent(50l);
		middle.setProgressTotal(100l);
		middleJSON =  EntityFactory.createJSONStringForEntity(middle);
		middleNotReady = new ResultNotReadyException(middleJSON);
		// done
		done = new AsynchronousJobStatus();
		done.setJobId(jobId);
		done.setJobState(AsynchJobState.COMPLETE);
		done.setProgressCurrent(100l);
		done.setProgressTotal(100l);
		doneJSON =  EntityFactory.createJSONStringForEntity(done);
		doneNotReady = new ResultNotReadyException(doneJSON);
		requestBody = new DownloadFromTableRequest();
		requestBody.setSql("select * from syn123");
		type = AsynchType.TableCSVDownload;
		
		responseBody = new DownloadFromTableResult();
		responseBody.setEtag("etag123");
		responseBody.setTableId("syn123");
		responseJSON = EntityFactory.createJSONStringForEntity(responseBody);
		
	}
	
	@Test
	public void testMultipleStatesThenSuccess() throws JSONObjectAdapterException{
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockSynapseClient).startAsynchJob(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		// simulate three calls
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, responseJSON).when(mockSynapseClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsyncCallback.class));
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
	public void testWithFailure() throws JSONObjectAdapterException{
		// Simulate start
		AsyncMockStubber.callSuccessWith(startJSON).when(mockSynapseClient).startAsynchJob(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		Throwable error = new Throwable("Something went wrong");
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		// It should also be updated when done
		verify(mockHandler, never()).onComplete(any(AsynchronousResponseBody.class));
		verify(mockHandler, never()).onCancel();
		// The error must be passed to the handler
		verify(mockHandler).onFailure(error);
	}
	
	@Test
	public void testMultipleStatesThenFailure() throws JSONObjectAdapterException{
		// Simulate start
		AsyncMockStubber.callSuccessWith(jobId).when(mockSynapseClient).startAsynchJob(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		// simulate three calls
		Throwable exception = new Throwable("Something went wrong");
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, exception).when(mockSynapseClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsyncCallback.class));
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
	public void testCancel() throws JSONObjectAdapterException{		
		// Simulate start
		AsyncMockStubber.callSuccessWith(startJSON).when(mockSynapseClient).startAsynchJob(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		// These will still be called ever after the cancel.
		AsyncMockStubber.callMixedWith(startNotReady, middleNotReady, doneNotReady, responseJSON).when(mockSynapseClient).getAsynchJobResults(any(AsynchType.class), anyString(), any(AsyncCallback.class));
		// Since this test is not using a multiple threads cancel must be called before we start.
		tracker.startAndTrack(type, requestBody, waitTimeMS, mockHandler);
		tracker.cancel();
		assertTrue(this.mockTimerProvider.isCancled());
		// Since cancel happens after complete, complete should still be called.
		verify(mockHandler).onComplete(any(AsynchronousResponseBody.class));
		// The handler should not get the onCancle() because the onComplete() would have already been sent.
		verify(mockHandler, never()).onCancel();
	}
}
