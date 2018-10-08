package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWikiWidgetView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class TableQueryResultWikiWidgetTest {
	TableQueryResultWikiWidget widget;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	TableQueryResultWikiWidgetView mockView;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	TableEntityWidget mockTableEntityWidget;
	@Mock
	ActionMenuWidget mockActionMenu;
	@Mock
	EntityActionController mockEntityActionController;
	@Mock
	EntityBundle mockEntityBundle;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new TableQueryResultWikiWidget(
				mockView, 
				mockTableEntityWidget,
				mockActionMenu,
				mockEntityActionController,
				mockSynapseJSNIUtils, 
				mockSynapseJavascriptClient, 
				mockSynAlert);
		AsyncMockStubber.callSuccessWith(mockEntityBundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setTableQueryResultWidget(any(Widget.class));
		verify(mockView).setSynAlert(any(Widget.class));
		verify(mockActionMenu).addControllerWidget(any(Widget.class));
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String tableId = "syn12345";
		String sql = "select * from " + tableId;
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		
		widget.configure(wikiKey, descriptor, null, null);
		
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(tableId), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		assertEquals((Long)TableEntityWidget.DEFAULT_LIMIT, query.getLimit());
		assertEquals((Long)TableEntityWidget.DEFAULT_OFFSET, query.getOffset());
		assertFalse(query.getIsConsistent());
		
		boolean isCurrentVersion = true;
		String wikiPageRootId = null;
		verify(mockEntityActionController).configure(mockActionMenu, mockEntityBundle, isCurrentVersion, wikiPageRootId, EntityArea.TABLES);
		boolean canEdit = false;
		verify(mockTableEntityWidget).configure(mockEntityBundle, canEdit, widget, mockActionMenu);
		
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		verify(mockTableEntityWidget, never()).hideFiltering();
	}
	
	@Test
	public void testConfigureNotFound() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		widget.configure(wikiKey, descriptor, null, null);
		InOrder inOrder = Mockito.inOrder(mockSynAlert);
		inOrder.verify(mockSynAlert).clear();
		inOrder.verify(mockSynAlert).handleException(isA(NotFoundException.class));
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
		
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		assertEquals(Long.valueOf(8080L), query.getLimit());
		assertEquals(Long.valueOf(333L), query.getOffset());
	}
	
	@Test
	public void testConfigureQueryVisible() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		descriptor.put(WidgetConstants.QUERY_VISIBLE, Boolean.TRUE.toString());
		
		widget.configure(wikiKey, descriptor, null, null);
		
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		verify(mockTableEntityWidget, never()).hideFiltering();
	}
	
	@Test
	public void testConfigureQueryNotVisible() {
		Map<String, String> descriptor = new HashMap<String, String>();
		String sql = "my query string";
		
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);
		descriptor.put(WidgetConstants.QUERY_VISIBLE, Boolean.FALSE.toString());
		
		widget.configure(wikiKey, descriptor, null, null);
		
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		verify(mockTableEntityWidget, times(2)).hideFiltering();
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
		
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		//should have been set to default values
		assertEquals((Long)TableEntityWidget.DEFAULT_LIMIT, query.getLimit());
		assertEquals((Long)TableEntityWidget.DEFAULT_OFFSET, query.getOffset());
		
		//log both errors to the console
		verify(mockSynapseJSNIUtils, times(2)).consoleError(anyString());
	}
}
