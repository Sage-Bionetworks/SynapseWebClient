package org.sagebionetworks.web.unitclient.widget.asynch;

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
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTrackerImpl;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsynchronousJobTrackerTest {

	SynapseClientAsync mockSynapseClient;
	TimerProvider mockTimerProvider;
	AdapterFactory adapterFactory;
	int waitTimeMS;
	UpdatingAsynchProgressHandler mockHandler;
	AsynchronousJobTrackerImpl tracker;
	AsynchronousJobStatus start;
	String startJSON;
	AsynchronousJobStatus middle;
	String middleJSON;
	AsynchronousJobStatus done;
	String doneJSON;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockTimerProvider = new TimerProviderStub();
		adapterFactory = new AdapterFactoryImpl();
		waitTimeMS = 1000;
		mockHandler = Mockito.mock(UpdatingAsynchProgressHandler.class);
		tracker = new AsynchronousJobTrackerImpl(mockSynapseClient, mockTimerProvider, adapterFactory);
		
		// Setup three phases for a job.
		String jobId = "123";
		// This job will have three phases.
		start = new AsynchronousJobStatus();
		start.setJobId(jobId);
		start.setJobState(AsynchJobState.PROCESSING);
		start.setProgressCurrent(0l);
		start.setProgressTotal(100l);
		startJSON = EntityFactory.createJSONStringForEntity(start);
		// In the middle
		middle = new AsynchronousJobStatus();
		middle.setJobId(jobId);
		middle.setJobState(AsynchJobState.PROCESSING);
		middle.setProgressCurrent(50l);
		middle.setProgressTotal(100l);
		middleJSON =  EntityFactory.createJSONStringForEntity(middle);
		// done
		done = new AsynchronousJobStatus();
		done.setJobId(jobId);
		done.setJobState(AsynchJobState.COMPLETE);
		done.setProgressCurrent(100l);
		done.setProgressTotal(100l);
		doneJSON =  EntityFactory.createJSONStringForEntity(done);
//		tracker = new AsynchronousJobTrackerImpl(mockSynapseClient, mockTimerProvider, adapterFactory, waitTimeMS, start, mockHandler);
	}
	
	@Test
	public void testAlreadyDone(){
		// Start with a job that is already done.
		start.setJobState(AsynchJobState.COMPLETE);
		tracker.configure(start, waitTimeMS, mockHandler);
		tracker.start();
		verify(mockHandler).onComplete(start);
	}
	
	@Test
	public void testAlreadyFailed(){
		// Start with a job that is already done.
		start.setJobState(AsynchJobState.FAILED);
		tracker.configure(start, waitTimeMS, mockHandler);
		tracker.start();
		verify(mockHandler).onComplete(start);
	}
	
	@Test
	public void testMultipleStatesSuccess() throws JSONObjectAdapterException{
		// simulate three calls
		AsyncMockStubber.callSuccessWith(startJSON, middleJSON, doneJSON).when(mockSynapseClient).getAsynchJobStatus(anyString(), any(AsyncCallback.class));
		tracker.configure(start, waitTimeMS, mockHandler);
		tracker.start();
		// Update should occur for all three phases
		verify(mockHandler, times(2)).onUpdate(start);
		verify(mockHandler).onUpdate(middle);
		verify(mockHandler).onUpdate(done);
		// It should also be updated when done
		verify(mockHandler).onComplete(done);
		verify(mockHandler, never()).onCancel(any(AsynchronousJobStatus.class));
		verify(mockHandler, never()).onStatusCheckFailure(anyString(), any(Throwable.class));
	}
	
	@Test
	public void testWithFailure() throws JSONObjectAdapterException{
		Throwable error = new Throwable("Something went wrong");
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).getAsynchJobStatus(anyString(), any(AsyncCallback.class));
		tracker.configure(start, waitTimeMS, mockHandler);
		start.setJobState(AsynchJobState.PROCESSING);
		tracker.start();
		// Even though the call will fail it should still call update once.
		verify(mockHandler).onUpdate(start);
		// It should also be updated when done
		verify(mockHandler, never()).onComplete(any(AsynchronousJobStatus.class));
		verify(mockHandler, never()).onCancel(any(AsynchronousJobStatus.class));
		// The error must be passed to the handler
		verify(mockHandler).onStatusCheckFailure(start.getJobId(), error);
	}
	
	@Test
	public void testCancel() throws JSONObjectAdapterException{		
		// simulate three calls
		// These will still be called ever after the cancel.
		AsyncMockStubber.callSuccessWith(startJSON, middleJSON, doneJSON).when(mockSynapseClient).getAsynchJobStatus(anyString(), any(AsyncCallback.class));
		tracker.configure(start, waitTimeMS, mockHandler);
		// Since this test is not using a multiple threads cancel must be called before we start.
		tracker.cancel();
		tracker.start();
		// Update should occur once
		verify(mockHandler).onUpdate(start);
		verify(mockHandler, never()).onUpdate(middle);
		verify(mockHandler, never()).onUpdate(done);
		// It should also be updated when done
		verify(mockHandler, never()).onComplete(any(AsynchronousJobStatus.class));
		// Cancel must be called
		verify(mockHandler).onCancel(start);
		verify(mockHandler, never()).onStatusCheckFailure(anyString(), any(Throwable.class));
	}
}
