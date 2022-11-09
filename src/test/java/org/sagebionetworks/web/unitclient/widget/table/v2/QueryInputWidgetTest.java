package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputView;
import org.sagebionetworks.web.client.widget.table.v2.QueryInputWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryInputListener;

public class QueryInputWidgetTest {

  QueryInputView mockView;
  SynapseClientAsync mockSynapseClient;
  QueryInputListener mockQueryInputListener;
  QueryInputWidget widget;

  @Before
  public void before() {
    mockView = Mockito.mock(QueryInputView.class);
    mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
    mockQueryInputListener = Mockito.mock(QueryInputListener.class);
    widget = new QueryInputWidget(mockView, mockSynapseClient);
  }

  @Test
  public void testConfigure() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    verify(mockView).setInputQueryString(sql);
    verify(mockView).setQueryInputLoading(false);
    verify(mockView).showInputError(false);
  }

  @Test
  public void testExecuteSuccess() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    when(mockView.getInputQueryString()).thenReturn(sql);

    widget.onExecuteQuery();

    verify(mockView).setQueryInputLoading(true);
    verify(mockView, times(2)).showInputError(false);
    verify(mockView).setQueryInputLoading(true);
    // the listener should be passed the query
    verify(mockQueryInputListener).onExecuteQuery(sql);
  }

  @Test
  public void testEmptyQuery() {
    String sql = "";
    widget.configure(sql, mockQueryInputListener);
    when(mockView.getInputQueryString()).thenReturn(sql);

    widget.onExecuteQuery();

    verify(mockView).setQueryInputLoading(true);
    verify(mockView).showInputError(false);
    verify(mockView).setQueryInputLoading(true);
    verify(mockView)
      .setInputErrorMessage(QueryInputWidget.AN_EMPTY_QUERY_IS_NOT_VALID);
    // the listener should not be passed the query
    verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
  }

  @Test
  public void testNullQuery() {
    String sql = null;
    widget.configure(sql, mockQueryInputListener);
    when(mockView.getInputQueryString()).thenReturn(sql);

    widget.onExecuteQuery();

    verify(mockView).setQueryInputLoading(true);
    verify(mockView).showInputError(false);
    verify(mockView).setQueryInputLoading(true);
    verify(mockView)
      .setInputErrorMessage(QueryInputWidget.AN_EMPTY_QUERY_IS_NOT_VALID);
    // the listener should not be passed the query
    verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
  }

  @Test
  public void testQueryWithNoEntityID() {
    String sql = "select name where x=y";
    widget.configure(sql, mockQueryInputListener);
    when(mockView.getInputQueryString()).thenReturn(sql);

    widget.onExecuteQuery();

    verify(mockView).setQueryInputLoading(true);
    verify(mockView).showInputError(false);
    verify(mockView).setQueryInputLoading(true);
    verify(mockView).setInputErrorMessage(QueryInputWidget.ENTITY_ID_NOT_FOUND);
    // the listener should not be passed the query
    verify(mockQueryInputListener, never()).onExecuteQuery(anyString());
  }

  @Test
  public void testOnReset() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    verify(mockView).setInputQueryString(sql);
    verify(mockView).setQueryInputLoading(false);
    verify(mockView).showInputError(false);
    reset(mockView);
    widget.onReset();
    verify(mockView).setInputQueryString(sql);
    verify(mockView).setQueryInputLoading(false);
    verify(mockView).showInputError(false);
  }

  @Test
  public void testQueryExecutionFinished() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    reset(mockView);
    boolean success = true;
    boolean resultsEditable = true;
    widget.queryExecutionFinished(success, resultsEditable);
    verify(mockView).setQueryInputLoading(false);
  }

  @Test
  public void testQueryExecutionFinishedResultsNotEditable() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    reset(mockView);
    boolean success = true;
    boolean resultsEditable = false;
    widget.queryExecutionFinished(success, resultsEditable);
    verify(mockView).setQueryInputLoading(false);
  }

  @Test
  public void testExecutionFinished() {
    String sql = "select * from syn123";
    widget.configure(sql, mockQueryInputListener);
    reset(mockView);
    widget.queryExecutionStarted();
    verify(mockView).setQueryInputLoading(true);
  }

  @Test
  public void testQueryInputVisible() {
    widget.setQueryInputVisible(true);
    verify(mockView).setQueryInputVisible(true);
  }
}
