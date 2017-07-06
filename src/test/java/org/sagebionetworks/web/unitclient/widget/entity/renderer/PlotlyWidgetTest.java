package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigEditor.*;
import static org.sagebionetworks.web.shared.WidgetConstants.*;
import static org.mockito.Matchers.*;
import static org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidget.*;
import static org.sagebionetworks.web.client.ClientProperties.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.plotly.PlotlyTrace;
import org.sagebionetworks.web.client.presenter.HomePresenter;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.PlotlyWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class PlotlyWidgetTest {
	PlotlyWidget widget;
	@Mock
	PlotlyWidgetView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	AsynchronousJobTracker mockJobTracker;
	@Captor
	ArgumentCaptor<QueryBundleRequest> queryBundleRequestCaptor;
	@Captor
	ArgumentCaptor<UpdatingAsynchProgressHandler> jobTrackerCallbackCaptor;
	@Mock
	QueryResultBundle mockQueryResultBundle;
	@Mock
	QueryResult mockQueryResult;
	@Mock
	RowSet mockRowSet;
	@Mock
	Row mockRow;
	@Mock
	ResourceLoader mockResourceLoader;
	
	List<SelectColumn> selectColumns;
	List<Row> rows;
	List<String> rowValues;
	
	@Mock
	SelectColumn mockXColumn;
	@Mock
	SelectColumn mockY1Column;
	@Mock
	SelectColumn mockY2Column;
	@Captor
	ArgumentCaptor<PlotlyTrace[]> plotlyTraceArrayCaptor;
	@Mock
	AsynchronousJobStatus mockAsynchronousJobStatus;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Captor
	ArgumentCaptor<AsyncCallback> webResourceLoadedCallbackCaptor;
	Map<String, String> params;
	public static final String X_COLUMN_NAME = "x";
	public static final String Y1_COLUMN_NAME = "y1";
	public static final String Y2_COLUMN_NAME = "y2";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		widget = new PlotlyWidget(mockView, mockSynAlert, mockJobTracker, mockResourceLoader);
		params = new HashMap<>();
		selectColumns = new ArrayList<>();
		rows = new ArrayList<>();
		rowValues = new ArrayList<>();
		
		when(mockQueryResultBundle.getSelectColumns()).thenReturn(selectColumns);
		when(mockQueryResultBundle.getQueryResult()).thenReturn(mockQueryResult);
		when(mockQueryResult.getQueryResults()).thenReturn(mockRowSet);
		when(mockRowSet.getRows()).thenReturn(rows);
		when(mockXColumn.getName()).thenReturn(X_COLUMN_NAME);
		when(mockY1Column.getName()).thenReturn(Y1_COLUMN_NAME);
		when(mockY2Column.getName()).thenReturn(Y1_COLUMN_NAME);
		when(mockRow.getValues()).thenReturn(rowValues);
		when(mockResourceLoader.isLoaded(eq(PLOTLY_JS))).thenReturn(true);
	}
	
	@Test
	public void testConstructor() {
		verify(mockView).setSynAlertWidget(mockSynAlert);
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		WikiPageKey pageKey = null;
		String xAxisLabel = "X Axis";
		String yAxisLabel = "Y Axis";
		GraphType type = GraphType.BAR;
		BarMode mode = BarMode.STACK;
		String plotTitle = "Plot Title";
		String sql = "select x, y1, y2 from syn12345 where x>2";
		params.put(TABLE_QUERY_KEY, sql);
		params.put(TITLE, plotTitle);
		params.put(X_AXIS_TITLE, xAxisLabel);
		params.put(Y_AXIS_TITLE, yAxisLabel);
		params.put(TYPE, type.toString());
		params.put(BAR_MODE, mode.toString());
		
		selectColumns.add(mockXColumn);
		selectColumns.add(mockY1Column);
		selectColumns.add(mockY2Column);
		rowValues.add("row1X");
		rowValues.add("1.1");
		rowValues.add("2.2");
		rows.add(mockRow);
		when(mockQueryResultBundle.getQueryCount()).thenReturn(LIMIT + 1);
		
		widget.configure(pageKey, params, null, null);
		
		//verify query params, and plot configuration based on results
		verify(mockView).setLoadingVisible(true);
		verify(mockJobTracker).startAndTrack(eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), eq(AsynchronousProgressWidget.WAIT_MS), jobTrackerCallbackCaptor.capture());
		//check query
		QueryBundleRequest request = queryBundleRequestCaptor.getValue();
		assertEquals(ALL_PARTS_MASK, request.getPartMask());
		assertEquals("syn12345", request.getEntityId());
		assertEquals((Long)0L, request.getQuery().getOffset());
		assertEquals(sql, request.getQuery().getSql());
		// complete first page load
		jobTrackerCallbackCaptor.getValue().onComplete(mockQueryResultBundle);
		
		verify(mockView, never()).showChart(eq(plotTitle), anyString(), anyString(), any(PlotlyTrace[].class), anyString(), anyString());
		
		//test final page
		rows.clear();
		verify(mockJobTracker, times(2)).startAndTrack(eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), eq(AsynchronousProgressWidget.WAIT_MS), jobTrackerCallbackCaptor.capture());
		//verify offset updated
		request = queryBundleRequestCaptor.getValue();
		assertEquals(LIMIT, request.getQuery().getOffset());
		
		verify(mockView, times(2)).setLoadingMessage(stringCaptor.capture());
		String loadingMessage = stringCaptor.getValue();
		assertTrue(loadingMessage.contains(LIMIT.toString()));
		
		jobTrackerCallbackCaptor.getValue().onComplete(mockQueryResultBundle);
		verify(mockView).showChart(eq(plotTitle), eq(xAxisLabel), eq(yAxisLabel), plotlyTraceArrayCaptor.capture(), eq(mode.toString().toLowerCase()), anyString());
		PlotlyTrace[] traceArray = plotlyTraceArrayCaptor.getValue();
		assertTrue(traceArray.length > 0);
		assertEquals(type.toString().toLowerCase(), traceArray[0].getType());
	}
	
	@Test
	public void testTrackerOnCancel() {
		GraphType type = GraphType.SCATTER;
		params.put(TYPE, type.toString());
		WikiPageKey pageKey = null;
		widget.configure(pageKey, params, null, null);
		verify(mockJobTracker).startAndTrack(eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), eq(AsynchronousProgressWidget.WAIT_MS), jobTrackerCallbackCaptor.capture());
		UpdatingAsynchProgressHandler progressHandler = jobTrackerCallbackCaptor.getValue();
		
		//also verify attachment state is based on view
		when(mockView.isAttached()).thenReturn(true);
		assertTrue(progressHandler.isAttached());
		when(mockView.isAttached()).thenReturn(false);
		assertFalse(progressHandler.isAttached());
		
		progressHandler.onCancel();
		verify(mockView, times(2)).clearChart();
		verify(mockView).setLoadingVisible(false);
	}
	
	@Test
	public void testTrackerOnFailure() {
		GraphType type = GraphType.SCATTER;
		params.put(TYPE, type.toString());
		WikiPageKey pageKey = null;
		widget.configure(pageKey, params, null, null);
		verify(mockJobTracker).startAndTrack(eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), eq(AsynchronousProgressWidget.WAIT_MS), jobTrackerCallbackCaptor.capture());
		UpdatingAsynchProgressHandler progressHandler = jobTrackerCallbackCaptor.getValue();
		
		Exception error = new Exception();
		progressHandler.onFailure(error);
		verify(mockView, times(2)).clearChart();
		verify(mockView).setLoadingVisible(false);
		verify(mockSynAlert).handleException(error);
	}
	
	@Test
	public void testLazyLoadPlotlyJS() throws JSONObjectAdapterException {
		GraphType type = GraphType.SCATTER;
		params.put(TYPE, type.toString());
		WikiPageKey pageKey = null;
		when(mockResourceLoader.isLoaded(eq(PLOTLY_JS))).thenReturn(false);
		widget.configure(pageKey, params, null, null);
		
		verify(mockJobTracker).startAndTrack(eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), eq(AsynchronousProgressWidget.WAIT_MS), jobTrackerCallbackCaptor.capture());
		jobTrackerCallbackCaptor.getValue().onComplete(mockQueryResultBundle);
		
		verify(mockView, never()).showChart(anyString(), anyString(), anyString(), any(PlotlyTrace[].class), anyString(), anyString());
		
		verify(mockResourceLoader).isLoaded(eq(PLOTLY_JS));
		verify(mockResourceLoader).requires(eq(PLOTLY_JS), webResourceLoadedCallbackCaptor.capture());
		
		AsyncCallback callback = webResourceLoadedCallbackCaptor.getValue();
		Exception ex = new Exception();
		callback.onFailure(ex);
		verify(mockSynAlert).handleException(ex);
		verify(mockView, never()).showChart(anyString(), anyString(), anyString(), any(PlotlyTrace[].class), anyString(), anyString());
		
		when(mockResourceLoader.isLoaded(eq(PLOTLY_JS))).thenReturn(true);
		callback.onSuccess(null);
		verify(mockView).showChart(anyString(), anyString(), anyString(), any(PlotlyTrace[].class), anyString(), anyString());
	}	


	@Test
	public void testTransformWithFill() {
		/**
		 * 
		 * Test the following table data
			| x | fill | y1  |
			|---|------|----|
			| 1 | a    | 40 |
			| 1 | b    | 50 |
			| 2 | a    | 60 |
		 * 
		 * For each y, there should be a new series for each fill value.
		 * So this should result in 2 series, one for 'a' and one for 'b'.
		 */
		String xAxisColumnName = "x";
		String y1ColumnName = "y1";
		
		String fillColumnName = "fill";
		GraphType graphType = GraphType.BAR;
		Map<String, List<String>> graphData = new HashMap<>();
		graphData.put(xAxisColumnName, Arrays.asList("1", "1", "2"));
		graphData.put(fillColumnName, Arrays.asList("a", "b", "a"));
		graphData.put(y1ColumnName, Arrays.asList("40", "50", "60"));
		PlotlyTrace[] traces = PlotlyWidget.transform(xAxisColumnName, fillColumnName, graphType, graphData);
		assertEquals(2, traces.length);
		PlotlyTrace a = traces[0].getName().equals("a") ? traces[0] : traces[1];
		assertEquals(2, a.getX().length);
		assertEquals("40", a.getY()[0]);
		assertEquals("60", a.getY()[1]);
	}
}
