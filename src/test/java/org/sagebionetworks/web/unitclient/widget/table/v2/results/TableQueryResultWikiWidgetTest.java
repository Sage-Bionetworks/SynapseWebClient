package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultsListener;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TableQueryResultWikiWidgetTest {
	TableQueryResultWikiWidget widget;
	TableQueryResultWidget mockTableQueryResultWidget;
	TableQueryResultWikiWidgetView mockView;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	EntityHeader mockEntityHeader;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockTableQueryResultWidget = mock(TableQueryResultWidget.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockView = mock(TableQueryResultWikiWidgetView.class);
		widget = new TableQueryResultWikiWidget(mockView, mockTableQueryResultWidget, mockSynapseJSNIUtils, mockSynapseClient, mockSynAlert);
		ArrayList<EntityHeader> entityHeaderList = new ArrayList<EntityHeader>();
		entityHeaderList.add(mockEntityHeader);
		AsyncMockStubber.callSuccessWith(entityHeaderList).when(mockSynapseClient).getEntityHeaderBatch(anyList(), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setTableQueryResultWidget(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
	}

	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		widget.configure(wikiKey, descriptor, null, null);
		ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
		verify(mockTableQueryResultWidget).configure(captor.capture(), eq(false), (QueryResultsListener)isNull());
		
		Query capturedQuery = captor.getValue();
		assertEquals(sql, capturedQuery.getSql());
		assertEquals((Long)TableEntityWidget.DEFAULT_LIMIT, capturedQuery.getLimit());
		assertEquals((Long)TableEntityWidget.DEFAULT_OFFSET, capturedQuery.getOffset());
	}
	
	@Test
	public void testConfigureNonDefaultLimitOffset() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		String limit = "8080";
		String offset = "333";
		
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		descriptor.put(WidgetConstants.TABLE_OFFSET_KEY, offset);
		descriptor.put(WidgetConstants.TABLE_LIMIT_KEY, limit);
		widget.configure(wikiKey, descriptor, null, null);
		ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
		verify(mockTableQueryResultWidget).configure(captor.capture(), eq(false), (QueryResultsListener)isNull());
		
		Query capturedQuery = captor.getValue();
		assertEquals(sql, capturedQuery.getSql());
		assertEquals(Long.valueOf(8080L), capturedQuery.getLimit());
		assertEquals(Long.valueOf(333L), capturedQuery.getOffset());
	}
	
	@Test
	public void testInvalidLimitOffset() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		String limit = "abc";
		String offset = "def";
		
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		descriptor.put(WidgetConstants.TABLE_OFFSET_KEY, offset);
		descriptor.put(WidgetConstants.TABLE_LIMIT_KEY, limit);
		widget.configure(wikiKey, descriptor, null, null);
		ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
		verify(mockTableQueryResultWidget).configure(captor.capture(), eq(false), (QueryResultsListener)isNull());
		
		Query capturedQuery = captor.getValue();
		assertEquals(sql, capturedQuery.getSql());
		//should have been set to default values
		assertEquals((Long)TableEntityWidget.DEFAULT_LIMIT, capturedQuery.getLimit());
		assertEquals((Long)TableEntityWidget.DEFAULT_OFFSET, capturedQuery.getOffset());
		
		//log both errors to the console
		verify(mockSynapseJSNIUtils, times(2)).consoleError(anyString());
	}
}
