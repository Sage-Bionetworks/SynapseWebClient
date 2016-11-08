package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TableQueryResultWidgetTest {
	
	TablePageWidget mockPageWidget;
	JobTrackingWidgetStub jobTrackingStub;
	QueryResultsListener mockListner;
	TableQueryResultView mockView;
	SynapseClientAsync mockSynapseClient;
	QueryResultEditorWidget mockQueryResultEditor;
	PortalGinInjector mockGinInjector;
	TableQueryResultWidget widget;
	Query query;
	QueryResultBundle bundle;
	PartialRowSet delta;
	SortItem sort;
	Row row;
	RowSet rowSet;
	QueryResult results;
	SelectColumn select;
	SynapseAlert mockSynapseAlert;
	boolean isView;
	@Mock
	CallbackP<FacetColumnRequest> mockFacetChangedHandler;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		jobTrackingStub = new JobTrackingWidgetStub();
		mockListner = Mockito.mock(QueryResultsListener.class);
		mockView = Mockito.mock(TableQueryResultView.class);
		mockPageWidget = Mockito.mock(TablePageWidget.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockQueryResultEditor = Mockito.mock(QueryResultEditorWidget.class);
		mockSynapseAlert = Mockito.mock(SynapseAlert.class);
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(jobTrackingStub);
		when(mockGinInjector.createNewTablePageWidget()).thenReturn(mockPageWidget);
		when(mockGinInjector.createNewQueryResultEditorWidget()).thenReturn(mockQueryResultEditor);
		widget = new TableQueryResultWidget(mockView, mockSynapseClient, mockGinInjector, mockSynapseAlert);
		query = new Query();
		query.setSql("select * from syn123");
		row = new Row();
		row.setRowId(123L);
		select = new SelectColumn();
		select.setId("123");
		rowSet = new RowSet();
		rowSet.setRows(Arrays.asList(row));
		rowSet.setHeaders(Arrays.asList(select));
		results = new QueryResult();
		results.setQueryResults(rowSet);
		bundle = new QueryResultBundle();
		bundle.setMaxRowsPerPage(123L);
		bundle.setQueryCount(88L);
		bundle.setQueryResult(results);
		
		sort = new SortItem();
		sort.setColumn("a");
		sort.setDirection(SortDirection.DESC);
		AsyncMockStubber.callSuccessWith(Arrays.asList(sort)).when(mockSynapseClient).getSortFromTableQuery(any(String.class),  any(AsyncCallback.class));
		
		// delta
		delta = new PartialRowSet();
		delta.setTableId("syn123");
		
		when(mockSynapseAlert.isUserLoggedIn()).thenReturn(true);
		isView = false;
	}
	
	@Test
	public void testConfigureSuccessEditable(){
		boolean isEditable = true;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), sort, false, isView, null, widget, mockFacetChangedHandler);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, true);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setSynapseAlertWidget(any(Widget.class));
	}
	
	@Test
	public void testConfigureNotLoggedIn() {
		boolean isEditable = false;
		when(mockSynapseAlert.isUserLoggedIn()).thenReturn(false);
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView).setTableVisible(false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockView).setSynapseAlertWidget(any(Widget.class));
		verify(mockSynapseAlert).showLogin();
	}
	
	@Test
	public void testConfigureSuccessNotEditable(){
		boolean isEditable = false;
		isView = true;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Make the call that changes it all.
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), sort, false, isView, null, widget, mockFacetChangedHandler);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, true);
		verify(mockView).setProgressWidgetVisible(false);	
	}
	
	@Test
	public void testConfigureSuccessResultsNotEditable(){
		boolean isEditable = false;
		// setup a success
		jobTrackingStub.setResponse(bundle);
		// Results are only editable if all of the select columns have IDs.
		select.setId(null);
		// Make the call that changes it all.
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView).setTableVisible(false);
		verify(mockPageWidget).configure(bundle, widget.getStartingQuery(), sort, false, isView, null, widget, mockFacetChangedHandler);
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockView).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, false);
		verify(mockView).setProgressWidgetVisible(false);	
	}
	
	@Test
	public void testConfigureCancel(){
		boolean isEditable = true;
		// Setup a cancel
		jobTrackingStub.setOnCancel(true);
		// Make the call that changes it all.
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false, false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockSynapseAlert).showError(TableQueryResultWidget.QUERY_CANCELED);
	}
	
	@Test
	public void testConfigurError(){
		boolean isEditable = true;
		// Setup a failure
		Throwable error = new Throwable("Failed!!");
		jobTrackingStub.setError(error);
		// Make the call that changes it all.
		widget.configure(query, isEditable, isView, mockListner);
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false, false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockView, times(2)).setTableVisible(false);
		verify(mockSynapseAlert).handleException(error);
	}
	
}
