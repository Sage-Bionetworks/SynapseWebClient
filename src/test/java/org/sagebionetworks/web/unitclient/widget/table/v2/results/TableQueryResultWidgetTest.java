package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.RowSelectionListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TableQueryResultWidgetTest {
	
	@Mock
	TablePageWidget mockPageWidget;
	@Mock
	QueryResultsListener mockListner;
	@Mock
	TableQueryResultView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	QueryResultEditorWidget mockQueryResultEditor;
	@Mock
	PortalGinInjector mockGinInjector;
	TableQueryResultWidget widget;
	Query query;
	QueryResultBundle bundle;
	PartialRowSet delta;
	List<SortItem> sortList;
	Row row;
	RowSet rowSet;
	QueryResult results;
	SelectColumn select;
	@Mock
	SynapseAlert mockSynapseAlert;
	TableType tableType;
	@Captor
	ArgumentCaptor<CallbackP<FacetColumnRequest>> mockFacetChangedHandlerCaptor;
	@Mock
	FacetColumnRequest mockFacetColumnRequest;
	@Mock
	FacetColumnRequest mockFacetColumnRequest2;
	@Mock
	FacetColumnRequest mockFacetColumnRequest3;
	@Mock
	ClientCache mockClientCache;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	AsynchronousProgressWidget mockJobTrackingWidget;
	@Mock
	AsynchronousProgressWidget mockJobTrackingWidget2;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<QueryBundleRequest> qbrCaptor;
	@Mock
	FacetColumnResult mockFacetColumnResult;
	@Mock
	ColumnModel mockColumnModel;
	@Mock
	SelectColumn mockSelectColumn;
	@Mock
	QueryResultBundle mockNewPageQueryResultBundle;
	
	public static final String ENTITY_ID = "syn123";
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(mockJobTrackingWidget, mockJobTrackingWidget2);
		when(mockGinInjector.createNewTablePageWidget()).thenReturn(mockPageWidget);
		when(mockGinInjector.createNewQueryResultEditorWidget()).thenReturn(mockQueryResultEditor);
		widget = new TableQueryResultWidget(mockView, mockSynapseClient, mockGinInjector, mockSynapseAlert, mockClientCache, mockGWT);
		query = new Query();
		query.setSql("select * from " + ENTITY_ID);
		query.setIsConsistent(true);
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
		bundle.setFacets(Collections.singletonList(mockFacetColumnResult));
		bundle.setColumnModels(Collections.singletonList(mockColumnModel));
		bundle.setSelectColumns(Collections.singletonList(mockSelectColumn));
		
		when(mockNewPageQueryResultBundle.getQueryResult()).thenReturn(results);
		sortList = new ArrayList<SortItem>();
		SortItem sort = new SortItem();
		sort.setColumn("a");
		sort.setDirection(SortDirection.DESC);
		sortList.add(sort);
		AsyncMockStubber.callSuccessWith(sortList).when(mockSynapseClient).getSortFromTableQuery(any(String.class),  any(AsyncCallback.class));
		
		// delta
		delta = new PartialRowSet();
		delta.setTableId("syn123");
		
		when(mockSynapseAlert.isUserLoggedIn()).thenReturn(true);
		tableType = TableType.table;
	}
	
	@Test
	public void testConfigureSuccessEditable(){
		boolean isEditable = true;
		
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), qbrCaptor.capture(), asyncProgressHandlerCaptor.capture());
		
		AsynchronousProgressHandler progressHandler1 = asyncProgressHandlerCaptor.getValue();
		// reconfigure before previous job is done (verify previous job is no longer tracked, it uses a new tracking widget).
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		AsynchronousProgressHandler progressHandler2 = asyncProgressHandlerCaptor.getValue();
		// verify invoking a success on the first job is a no-op
		progressHandler1.onComplete(bundle);
		verify(mockPageWidget, never()).configure(any(QueryResultBundle.class), any(Query.class), anyList(), anyBoolean(), any(TableType.class), any(RowSelectionListener.class), any(TableQueryResultWidget.class), any(CallbackP.class), any(Callback.class));
		
		// invoke a success on the current job, and verify results sent to subwidgets
		progressHandler2.onComplete(bundle);
		
		verify(mockView, times(3)).setErrorVisible(false);
		verify(mockView, times(2)).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockPageWidget, times(2)).setTableVisible(false);
		verify(mockPageWidget).configure(eq(bundle), eq(widget.getStartingQuery()), eq(sortList), eq(false), eq(tableType), any(RowSelectionListener.class), eq(widget), mockFacetChangedHandlerCaptor.capture(), callbackCaptor.capture());
		verify(mockListner, times(2)).queryExecutionStarted();
		// Shown on success.
		verify(mockPageWidget).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, true);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setSynapseAlertWidget(any(Widget.class));
		
		// test facetChangeRequestHandler
		CallbackP<FacetColumnRequest> facetChangeRequestHandler = mockFacetChangedHandlerCaptor.getValue();
		assertNull(query.getSelectedFacets());
		String facetColumnName = "country";
		when(mockFacetColumnRequest.getColumnName()).thenReturn(facetColumnName);
		facetChangeRequestHandler.invoke(mockFacetColumnRequest);
		assertEquals(1, query.getSelectedFacets().size());
		assertEquals(mockFacetColumnRequest, query.getSelectedFacets().get(0));
		
		//verify that if the column name is the same, then the selected facet is updated for that column
		when(mockFacetColumnRequest2.getColumnName()).thenReturn(facetColumnName);
		facetChangeRequestHandler.invoke(mockFacetColumnRequest2);
		assertEquals(1, query.getSelectedFacets().size());
		assertEquals(mockFacetColumnRequest2, query.getSelectedFacets().get(0));
		
		//but if it's a facet for a different column, then both should be included.
		when(mockFacetColumnRequest3.getColumnName()).thenReturn("different column");
		facetChangeRequestHandler.invoke(mockFacetColumnRequest3);
		assertEquals(2, query.getSelectedFacets().size());
		assertTrue(query.getSelectedFacets().contains(mockFacetColumnRequest2));
		assertTrue(query.getSelectedFacets().contains(mockFacetColumnRequest3));
		
		// test reset facets handler
		Callback resetFacetsHandler = callbackCaptor.getValue();
		resetFacetsHandler.invoke();
		assertNull(query.getSelectedFacets());
	}
	
	@Test
	public void testOnPageChange(){
		boolean isEditable = true;
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), qbrCaptor.capture(), asyncProgressHandlerCaptor.capture());
		
		//verify all parts are initially asked for
		Long partsMask = qbrCaptor.getValue().getPartMask();
		Long expectedPartsMask = BUNDLE_MASK_QUERY_RESULTS | BUNDLE_MASK_QUERY_COLUMN_MODELS | BUNDLE_MASK_QUERY_SELECT_COLUMNS | BUNDLE_MASK_QUERY_FACETS;
		assertEquals(expectedPartsMask, partsMask);
		
		// verify the cache is being used:
		// clear it, then verify the rpc to get SortItems is called only once for this sql (on page change)
		TableQueryResultWidget.SQL_2_SORT_ITEMS_CACHE.clear();
		
		//simulate complete table query async job
		AsynchronousProgressHandler progressHandler1 = asyncProgressHandlerCaptor.getValue();
		progressHandler1.onComplete(bundle);
		verify(mockSynapseClient).getSortFromTableQuery(any(String.class),  any(AsyncCallback.class));
		
		// go to the next page
		Long newOffset = 25L;
		widget.onPageChange(newOffset);
		
		//only called once (on previous page load) because sql was in the sql2SortItems cache
		verify(mockSynapseClient).getSortFromTableQuery(any(String.class),  any(AsyncCallback.class));
		verify(mockView).scrollTableIntoView();
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), qbrCaptor.capture(), asyncProgressHandlerCaptor.capture());
		// verify we are not asking for the cached result values (column models, select columns, facets)
		partsMask = qbrCaptor.getValue().getPartMask();
		expectedPartsMask = BUNDLE_MASK_QUERY_RESULTS;
		assertEquals(expectedPartsMask, partsMask);
		AsynchronousProgressHandler progressHandler2 = asyncProgressHandlerCaptor.getValue();
		progressHandler2.onComplete(mockNewPageQueryResultBundle);
		//verify cached results are set on the new result
		verify(mockNewPageQueryResultBundle).setColumnModels(bundle.getColumnModels());
		verify(mockNewPageQueryResultBundle).setFacets(bundle.getFacets());
		verify(mockNewPageQueryResultBundle).setSelectColumns(bundle.getSelectColumns());
	}
	
	@Test
	public void testFacetsNotVisible(){
		boolean isEditable = true;
		widget.setFacetsVisible(false);
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), qbrCaptor.capture(), asyncProgressHandlerCaptor.capture());

		//verify all parts are initially asked for
		Long partsMask = qbrCaptor.getValue().getPartMask();
		Long expectedPartsMask = BUNDLE_MASK_QUERY_RESULTS | BUNDLE_MASK_QUERY_COLUMN_MODELS | BUNDLE_MASK_QUERY_SELECT_COLUMNS;
		assertEquals(expectedPartsMask, partsMask);
	}
	
	@Test
	public void testConfigureSuccessNotEditable(){
		boolean isEditable = false;
		tableType = TableType.fileview;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		// invoke a success
		asyncProgressHandlerCaptor.getValue().onComplete(bundle);
		
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockPageWidget).setTableVisible(false);
		verify(mockPageWidget).configure(eq(bundle), eq(widget.getStartingQuery()), eq(sortList), eq(false), eq(tableType), any(RowSelectionListener.class), eq(widget), mockFacetChangedHandlerCaptor.capture(), any(Callback.class));
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockPageWidget).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, true);
		verify(mockView).setProgressWidgetVisible(false);	
	}
	
	@Test
	public void testConfigureEmptyResultsIsConsistentFalse(){
		//no rows are returned, and isConsistent is false.  Verify it retries query in this case.
		boolean isEditable = false;
		tableType = TableType.fileview;
		query.setIsConsistent(false);
		rowSet.setRows(Collections.EMPTY_LIST);
		
		widget.configure(query, isEditable, tableType, mockListner);
		
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		asyncProgressHandlerCaptor.getValue().onComplete(bundle);
		
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), any(AsynchronousProgressHandler.class));
		//verify isConsistent has been flipped to true due to the empty result
		assertTrue(query.getIsConsistent());
	}
	
	@Test
	public void testConfigureErrorIsConsistentFalse(){
		boolean isEditable = true;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		query.setIsConsistent(false);
		
		// Setup a failure
		UnauthorizedException error = new UnauthorizedException("Failed!!");
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		// invoke the error
		asyncProgressHandlerCaptor.getValue().onFailure(error);
		
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), any(AsynchronousProgressHandler.class));
		//verify isConsistent has been flipped to true due to the empty result
		assertTrue(query.getIsConsistent());
	}
	
	@Test
	public void testConfigureSuccessResultsNotEditable(){
		boolean isEditable = false;
		// Results are only editable if all of the select columns have IDs.
		select.setId(null);
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		// invoke a success
		asyncProgressHandlerCaptor.getValue().onComplete(bundle);
		
		verify(mockView, times(2)).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockPageWidget).setTableVisible(false);
		verify(mockPageWidget).configure(eq(bundle), eq(widget.getStartingQuery()), eq(sortList), eq(false), eq(tableType), any(RowSelectionListener.class), eq(widget), mockFacetChangedHandlerCaptor.capture(), any(Callback.class));
		verify(mockListner).queryExecutionStarted();
		// Shown on success.
		verify(mockPageWidget).setTableVisible(true);
		verify(mockListner).queryExecutionFinished(true, false);
		verify(mockView).setProgressWidgetVisible(false);	
	}
	
	@Test
	public void testConfigureCancel(){
		boolean isEditable = true;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		// invoke a cancel
		asyncProgressHandlerCaptor.getValue().onCancel();
		
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockPageWidget, times(2)).setTableVisible(false);
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false, false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockPageWidget, times(2)).setTableVisible(false);
		verify(mockSynapseAlert).showError(TableQueryResultWidget.QUERY_CANCELED);
	}
	
	@Test
	public void testConfigurError(){
		boolean isEditable = true;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		
		// Setup a failure
		UnauthorizedException error = new UnauthorizedException("Failed!!");
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		// invoke the error
		asyncProgressHandlerCaptor.getValue().onFailure(error);
		
		verify(mockView).setErrorVisible(false);
		verify(mockView).setProgressWidgetVisible(true);
		// Hidden while running query.
		verify(mockPageWidget, times(2)).setTableVisible(false);
		verify(mockListner).queryExecutionStarted();
		// After a cancel
		verify(mockListner).queryExecutionFinished(false, false);
		verify(mockView).setProgressWidgetVisible(false);
		verify(mockView).setErrorVisible(true);
		verify(mockPageWidget, times(2)).setTableVisible(false);
		// note that if not logged in, synapse alert will show prompt the user to login in order to run the query.
		verify(mockSynapseAlert).handleException(error);
	}
	
	@Test
	public void testSetFacetsVisible() {
		widget.setFacetsVisible(true);
		verify(mockPageWidget).setFacetsVisible(true);
	}
	
	@Test
	public void testVerifyEtagUpdated() {
		String etag = "45678765-8765-876";
		when(mockClientCache.get(ENTITY_ID + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY)).thenReturn(etag);
		
		boolean isEditable = true;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.VERIFYING_ETAG_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		bundle.setQueryCount(0L);
		when(mockClientCache.get(ENTITY_ID + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY)).thenReturn(null);
		asyncProgressHandlerCaptor.getValue().onComplete(bundle);
		verify(mockClientCache).remove(ENTITY_ID + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
	}
	
	@Test
	public void testVerifyEtagOld() {
		String etag = "45678765-8765-876";
		when(mockClientCache.get(ENTITY_ID + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY)).thenReturn(etag);
		
		boolean isEditable = true;
		// Make the call that changes it all.
		widget.configure(query, isEditable, tableType, mockListner);
		
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.VERIFYING_ETAG_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
		bundle.setQueryCount(1L);
		asyncProgressHandlerCaptor.getValue().onComplete(bundle);
		verify(mockClientCache, never()).remove(ENTITY_ID + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
		verify(mockGWT).scheduleExecution(callbackCaptor.capture(), eq(TableQueryResultWidget.ETAG_CHECK_DELAY_MS));
		// manually invoke callback (usually would occur after the delay)
		callbackCaptor.getValue().invoke();
		verify(mockJobTrackingWidget2).startAndTrackJob(eq(TableQueryResultWidget.VERIFYING_ETAG_MESSAGE), eq(false), eq(AsynchType.TableQuery), any(QueryBundleRequest.class), asyncProgressHandlerCaptor.capture());
	}
	
}
