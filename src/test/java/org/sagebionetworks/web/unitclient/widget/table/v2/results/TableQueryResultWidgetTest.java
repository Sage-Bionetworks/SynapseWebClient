package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListner;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableQueryResultWidgetTest {
	
	TablePageWidget mockPageWidget;
	JobTrackingWidgetStub jobTrackingStub;
	QueryResultsListner mockListner;
	TableQueryResultView mockView;
	SynapseClientAsync mockSynapseClient;
	QueryResultEditorWidget mockQueryResultEditor;
	PortalGinInjector mockGinInjector;
	AdapterFactory adapterFactory;
	TableQueryResultWidget widget;
	Query query;
	QueryResultBundle bundle;
	PartialRowSet delta;
	
	@Before
	public void before(){
		jobTrackingStub = new JobTrackingWidgetStub();
		mockListner = Mockito.mock(QueryResultsListner.class);
		mockView = Mockito.mock(TableQueryResultView.class);
		mockPageWidget = Mockito.mock(TablePageWidget.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockQueryResultEditor = Mockito.mock(QueryResultEditorWidget.class);
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(jobTrackingStub);
		when(mockGinInjector.createNewTablePageWidget()).thenReturn(mockPageWidget);
		when(mockGinInjector.createNewQueryResultEditorWidget()).thenReturn(mockQueryResultEditor);
		adapterFactory = new AdapterFactoryImpl();
		widget = new TableQueryResultWidget(mockView, mockSynapseClient, mockGinInjector, adapterFactory);
		query = new Query();
		query.setSql("select * from syn123");
		bundle = new QueryResultBundle();
		bundle.setMaxRowsPerPage(123L);
		bundle.setQueryCount(88L);
		
		// delta
		delta = new PartialRowSet();
		delta.setTableId("syn123");
	}
	
	@Test
	public void testConfigureSuccessEditable(){
		boolean isEditable = true;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockView).hideEditor();
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), false, null, widget);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true);
		verify(mockView).setProgressWidgetVisible(false);
	}
	
	@Test
	public void testConfigureSuccessNotEditable(){
		boolean isEditable = false;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockView).hideEditor();
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), false, null, widget);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true);
		verify(mockView).setProgressWidgetVisible(false);	
	}
	
	@Test
	public void testConfigureCancel(){
		boolean isEditable = true;
		// Setup a cancel
		jobTrackingStub.setOnCancel(true);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockView).hideEditor();
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockView).showError(TableQueryResultWidget.QUERY_CANCELED);
	}
	
	@Test
	public void testConfigurError(){
		boolean isEditable = true;
		// Setup a failure
		Throwable error = new Throwable("Failed!!");
		jobTrackingStub.setError(error);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockView).hideEditor();
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockView).showError(error.getMessage());
	}
	
	@Test
	public void testOnSaveSuccess(){
		boolean isEditable = true;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		widget.onEditRows();
		verify(mockView).setEditorWidget(mockQueryResultEditor);
		verify(mockQueryResultEditor).configure(bundle);
		verify(mockView).setSaveButtonLoading(false);
		verify(mockView).showEditor();
		// Setup success
		when(mockQueryResultEditor.extractDelta()).thenReturn(delta);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).applyTableDelta(any(PartialRowSet.class),  any(AsyncCallback.class));
		// reset mocks
		reset(mockView);
		reset(mockPageWidget);
		reset(mockListner);
		// Now save
		widget.onSave();
		// It should re-run the query.
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockView).hideEditor();
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), false, null, widget);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true);
		verify(mockView).setProgressWidgetVisible(false);
	}
	
	@Test
	public void testOnSaveFailure(){
		boolean isEditable = true;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, mockListner);
		widget.onEditRows();
		verify(mockView).setEditorWidget(mockQueryResultEditor);
		verify(mockQueryResultEditor).configure(bundle);
		verify(mockView).setSaveButtonLoading(false);
		verify(mockView).showEditor();
		// Setup success
		when(mockQueryResultEditor.extractDelta()).thenReturn(delta);
		Throwable error = new Throwable("Things went bad!");
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).applyTableDelta(any(PartialRowSet.class),  any(AsyncCallback.class));
		// reset mocks
		reset(mockView);
		reset(mockPageWidget);
		reset(mockListner);
		// Now save
		widget.onSave();
		// Failures should not close the editor.
		verify(mockView, never()).hideEditor();
		verify(mockView).setSaveButtonLoading(false);
		// the editor should show 
		verify(mockQueryResultEditor).showError(error.getMessage());
		// The error message goes to the dialog.
		verify(mockView, never()).showError(anyString());
	}
}
