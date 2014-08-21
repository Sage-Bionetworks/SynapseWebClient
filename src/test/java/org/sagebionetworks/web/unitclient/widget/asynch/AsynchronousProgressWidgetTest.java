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
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.NumberFormatProvider;

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

	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockView = Mockito.mock(AsynchronousProgressView.class);
		numberFormatProvider = Mockito.mock(NumberFormatProvider.class);
		mockHandler = Mockito.mock(AsynchronousProgressHandler.class);
		states = new LinkedList<AsynchronousJobStatus>();
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
		
		trackerStub = new AsynchronousJobTrackerStub(states, null);
		widget = new AsynchronousProgressWidget(mockView, numberFormatProvider, trackerStub);
		
	}
	
	@Test
	public void testHappy(){
		String title = "title";
		widget.configure(title, start, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(done);
	}
	
	@Test
	public void testNullProgress(){
		String title = "title";
		start.setProgressCurrent(null);
		start.setProgressTotal(null);
		widget.configure(title, start, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(done);
	}
	
	@Test
	public void testDivideByZero(){
		String title = "title";
		start.setProgressCurrent(1l);
		start.setProgressTotal(0l);
		widget.configure(title, start, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setProgress(100.00, "100.00%", done.getProgressMessage());
		verify(mockHandler).onComplete(done);
	}
	
	@Test
	public void testCancel(){
		String title = "title";
		start.setProgressCurrent(1l);
		start.setProgressTotal(0l);
		widget.configure(title, start, mockHandler);
		verify(mockView).setTitle(title);
		verify(mockView).setProgress(0.0, "0.00%", start.getProgressMessage());
		verify(mockView).setProgress(50.00, "50.00%", middle.getProgressMessage());
		verify(mockView).setProgress(100.00, "100.00%", done.getProgressMessage());
		widget.onCancel();
		verify(mockHandler).onCancel(start);
	}
	
	@Test
	public void testError(){
		Throwable error = new Throwable("some error");
		trackerStub = new AsynchronousJobTrackerStub(states, error);
		widget = new AsynchronousProgressWidget(mockView, numberFormatProvider, trackerStub);
		widget.configure("title", start, mockHandler);
		verify(mockHandler).onStatusCheckFailure("123", error);
	}
}
