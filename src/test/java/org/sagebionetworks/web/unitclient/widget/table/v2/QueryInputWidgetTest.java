package org.sagebionetworks.web.unitclient.widget.table.v2;

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
import org.sagebionetworks.table.query.ParseException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputView;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class QueryInputWidgetTest {

	QueryInputView mockView;
	SynapseClientAsync mockSynapseClient;
	QueryInputListener mockQueryInputListener;
	QueryInputWidget widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(QueryInputView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockQueryInputListener = Mockito.mock(QueryInputListener.class);
		widget = new QueryInputWidget(mockView, mockSynapseClient);
	}
	
	@Test
	public void testConfigure(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		verify(mockView).setInputQueryString(sql);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).showInputError(false);
		verify(mockView).setEditVisible(true);
		verify(mockView).setEditEnabled(false);
		verify(mockView).setDownloadEnabled(false);
	}
	
	@Test
	public void testConfigureNotEditable(){
		boolean editable = false;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		verify(mockView).setInputQueryString(sql);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).showInputError(false);
		verify(mockView).setEditVisible(false);
		verify(mockView).setEditEnabled(false);
		verify(mockView).setDownloadEnabled(false);
	}
	
	@Test
	public void testExecuteValidateSuccess(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		when(mockView.getInputQueryString()).thenReturn(sql);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).validateTableQuery(anyString(), any(AsyncCallback.class));
		widget.onExecuteQuery();
		verify(mockView).setQueryInputLoading(true);
		verify(mockView, times(2)).showInputError(false);
		verify(mockView).setQueryInputLoading(true);
		// the listener should be passed the query
		verify(mockQueryInputListener).onExecuteQuery(sql);
	}
	
	
	@Test
	public void testExecuteValidateFailure(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		when(mockView.getInputQueryString()).thenReturn(sql);
		String errorMessage = "bad query";
		AsyncMockStubber.callFailureWith(new ParseException(errorMessage)).when(mockSynapseClient).validateTableQuery(anyString(), any(AsyncCallback.class));
		widget.onExecuteQuery();
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).showInputError(false);
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).setInputErrorMessage(errorMessage);
		// the listener should not be passed the query
		verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
	}
	
	@Test
	public void testEmptyQuery(){
		boolean editable = true;
		String sql = "";
		widget.configure(sql, mockQueryInputListener, editable);
		when(mockView.getInputQueryString()).thenReturn(sql);
		String errorMessage = "bad query";
		AsyncMockStubber.callFailureWith(new ParseException(errorMessage)).when(mockSynapseClient).validateTableQuery(anyString(), any(AsyncCallback.class));
		widget.onExecuteQuery();
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).showInputError(false);
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).setInputErrorMessage(QueryInputWidget.AN_EMPTY_QUERY_IS_NOT_VALID);
		// the listener should not be passed the query
		verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
		verify(mockSynapseClient, never()).validateTableQuery(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testNullQuery(){
		boolean editable = true;
		String sql = null;
		widget.configure(sql, mockQueryInputListener, editable);
		when(mockView.getInputQueryString()).thenReturn(sql);
		String errorMessage = "bad query";
		AsyncMockStubber.callFailureWith(new ParseException(errorMessage)).when(mockSynapseClient).validateTableQuery(anyString(), any(AsyncCallback.class));
		widget.onExecuteQuery();
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).showInputError(false);
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).setInputErrorMessage(QueryInputWidget.AN_EMPTY_QUERY_IS_NOT_VALID);
		// the listener should not be passed the query
		verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
		verify(mockSynapseClient, never()).validateTableQuery(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnReset(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		verify(mockView).setInputQueryString(sql);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).showInputError(false);
		reset(mockView);
		widget.onReset();
		verify(mockView).setInputQueryString(sql);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).showInputError(false);
		verify(mockView).setEditEnabled(false);
		verify(mockView).setDownloadEnabled(false);
	}
	
	@Test
	public void testQueryExecutionFinished(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		reset(mockView);
		boolean success = true;
		boolean resultsEditable = true;
		widget.queryExecutionFinished(success, resultsEditable);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).setEditEnabled(true);
		verify(mockView).setDownloadEnabled(true);
	}
	
	@Test
	public void testQueryExecutionFinishedResultsNotEditable(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		reset(mockView);
		boolean success = true;
		boolean resultsEditable = false;
		widget.queryExecutionFinished(success, resultsEditable);
		verify(mockView).setQueryInputLoading(false);
		verify(mockView).setEditEnabled(false);
		// donwload should still be enabled for this case
		verify(mockView).setDownloadEnabled(true);
	}

	@Test
	public void testExecutionFinished(){
		boolean editable = true;
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, editable);
		reset(mockView);
		widget.queryExecutionStarted();
		verify(mockView).setQueryInputLoading(true);
		verify(mockView).setEditEnabled(false);
		verify(mockView).setDownloadEnabled(false);
	}
	
	@Test
	public void testQueryInputVisible(){
		widget.setQueryInputVisible(true);
		verify(mockView).setQueryInputVisible(true);
	}

	@Test
	public void testShowQueryVisible(){
		boolean visible = true;
		widget.setShowQueryVisible(visible);
		verify(mockView).setShowQueryVisible(visible);
	}
	
	@Test
	public void testOnShowQuery(){
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, false);
		widget.onShowQuery();
		verify(mockQueryInputListener).onShowQuery();
	}
	@Test
	public void testOnDownlaod(){
		String sql = "select * from syn123";
		widget.configure(sql, mockQueryInputListener, false);
		widget.onDownloadFilesProgrammatically();
		verify(mockQueryInputListener).onShowDownloadFilesProgrammatically();
	}
}
