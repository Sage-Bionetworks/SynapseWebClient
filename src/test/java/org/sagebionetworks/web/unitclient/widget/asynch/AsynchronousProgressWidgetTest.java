package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.asynch.AsynchJobState;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProvider;
import org.sagebionetworks.web.shared.asynch.AsynchType;

/**
 * Business logic tests for AsynchronousProgressWidget.
 * @author John
 *
 */
public class AsynchronousProgressWidgetTest {
	
	AsynchronousProgressView mockView;
	NumberFormatProvider numberFormatProvider;
	AsynchronousProgressWidget widget;
	AsynchronousProgressHandler mockHandler;
	AsynchronousJobTracker trackerStub;
	AsynchronousJobStatus start;
	AsynchronousJobStatus middle;
	AsynchronousJobStatus done;
	List<AsynchronousJobStatus> states;
	DownloadFromTableRequest requestBody;
	DownloadFromTableResult responseBody;
	AsynchType type;
	String tableId;
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockView = Mockito.mock(AsynchronousProgressView.class);
		numberFormatProvider = Mockito.mock(NumberFormatProvider.class);
		mockHandler = Mockito.mock(AsynchronousProgressHandler.class);
		states = new LinkedList<AsynchronousJobStatus>();
		requestBody = new DownloadFromTableRequest();
		responseBody = new DownloadFromTableResult();
		tableId = "syn123";
		type = AsynchType.TableCSVDownload;
		requestBody.setSql("select * from " + tableId);
		requestBody.setEntityId(tableId);
		// Setup three phases for a job.
		String jobId = "123";
		// This job will have three phases.
		start = new AsynchronousJobStatus();
		start.setJobId(jobId);
		start.setJobState(AsynchJobState.PROCESSING);
		start.setProgressCurrent(0l);
		start.setProgressTotal(100l);
		start.setProgressMessage("Starting...");
		states.add(start);
		// In the middle
		middle = new AsynchronousJobStatus();
		middle.setJobId(jobId);
		middle.setJobState(AsynchJobState.PROCESSING);
		middle.setProgressCurrent(50l);
		middle.setProgressTotal(100l);
		middle.setProgressMessage("Middle...");
		states.add(middle);
		// done
		done = new AsynchronousJobStatus();
		done.setJobId(jobId);
		done.setJobState(AsynchJobState.COMPLETE);
		done.setProgressCurrent(100l);
		done.setProgressTotal(100l);
		done.setProgressMessage("Done...");
		states.add(done);
		
		when(numberFormatProvider.format(0.0)).thenReturn("0.00");
		when(numberFormatProvider.format(50.0)).thenReturn("50.00");
		when(numberFormatProvider.format(100.0)).thenReturn("100.00");
		
		trackerStub = new AsynchronousJobTrackerStub(states, null, responseBody);
		widget = new AsynchronousProgressWidget(mockView, numberFormatProvider, trackerStub);
		
	}
	
	@Test
	public void testHappy(){
		String title = "title";
		widget.startAndTrackJob(title, true, type, requestBody, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setDeterminateProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setDeterminateProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setDeterminateProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(responseBody);
	}
	
	@Test
	public void testNullProgress(){
		String title = "title";
		start.setProgressCurrent(null);
		start.setProgressTotal(null);
		widget.startAndTrackJob(title, true, type, requestBody, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setDeterminateProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setDeterminateProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setDeterminateProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(responseBody);
	}
	
	@Test
	public void testDivideByZero(){
		String title = "title";
		start.setProgressCurrent(1l);
		start.setProgressTotal(0l);
		widget.startAndTrackJob(title, true, type, requestBody, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setDeterminateProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setDeterminateProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setDeterminateProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(responseBody);
	}
	
	@Test
	public void testError(){
		Throwable error = new Throwable("some error");
		trackerStub = new AsynchronousJobTrackerStub(states, error, responseBody);
		widget = new AsynchronousProgressWidget(mockView, numberFormatProvider, trackerStub);
		widget.startAndTrackJob("title", true, type, requestBody, mockHandler);
		verify(mockHandler).onFailure(error);
	}
	
	@Test
	public void testIndeterminate(){
		String title = "title";
		start.setProgressCurrent(1l);
		start.setProgressTotal(0l);
		widget.startAndTrackJob(title,false, type, requestBody, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setIndetermianteProgress(start.getProgressMessage());
		verify(mockView).setIndetermianteProgress(middle.getProgressMessage());
		verify(mockView).setIndetermianteProgress(done.getProgressMessage());
	}
}
