package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
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

@RunWith(MockitoJUnitRunner.class)
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
	Long versionNumber;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	GWTWrapper mockGWT;
	@Captor
	ArgumentCaptor<AsyncCallback> asyncCallbackCaptor;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;

	@Before
	public void before() {
		widget = new TableQueryResultWikiWidget(mockView, mockActionMenu, mockEntityActionController, mockSynapseJSNIUtils, mockSynapseJavascriptClient, mockSynAlert, mockGWT, mockGinInjector);
		AsyncMockStubber.callSuccessWith(mockEntityBundle).when(mockSynapseJavascriptClient).getEntityBundleFromCache(anyString(), any(AsyncCallback.class));
		when(mockGinInjector.createNewTableEntityWidget()).thenReturn(mockTableEntityWidget);
	}

	@Test
	public void testConstruction() {
		// lazily loaded table query result widget
		verify(mockView, never()).setTableQueryResultWidget(any(Widget.class));
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
		Long tableVersionNumber = 22L;
		String sql = "select * from " + tableId + "." + tableVersionNumber;
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, sql);

		widget.configure(wikiKey, descriptor, null, null);

		verifyZeroInteractions(mockGWT);
		verify(mockView).setTableQueryResultWidget(any(Widget.class));
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(tableId), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		Query query = widget.getQueryString();
		assertEquals(sql, query.getSql());
		assertEquals((Long) TableEntityWidget.DEFAULT_LIMIT, query.getLimit());
		assertEquals((Long) TableEntityWidget.DEFAULT_OFFSET, query.getOffset());
		assertFalse(query.getIsConsistent());

		boolean isCurrentVersion = true;
		String wikiPageRootId = null;
		verify(mockEntityActionController).configure(mockActionMenu, mockEntityBundle, isCurrentVersion, wikiPageRootId, EntityArea.TABLES);
		boolean canEdit = false;
		verify(mockTableEntityWidget).configure(mockEntityBundle, tableVersionNumber, canEdit, widget, mockActionMenu);

		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.UPLOAD_TABLE_DATA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.EDIT_TABLE_DATA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		verify(mockActionMenu, atLeastOnce()).setActionVisible(Action.SHOW_TABLE_SCHEMA, false);
		verify(mockTableEntityWidget, never()).hideFiltering();
	}

	@Test
	public void testConfigureNotFound() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseJavascriptClient).getEntityBundleFromCache(anyString(), any(AsyncCallback.class));
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
		// should have been set to default values
		assertEquals((Long) TableEntityWidget.DEFAULT_LIMIT, query.getLimit());
		assertEquals((Long) TableEntityWidget.DEFAULT_OFFSET, query.getOffset());

		// log both errors to the console
		verify(mockSynapseJSNIUtils, times(2)).consoleError(anyString());
	}

	@Test
	public void testLoadMultipleTables() {
		// do not typically use reset(), but other tests need mockSynapseJavascriptClient response. in this
		// test, we do not want it to respond (until we manually invoke)
		reset(mockSynapseJavascriptClient);
		TableQueryResultWikiWidget widget1 = new TableQueryResultWikiWidget(mockView, mockActionMenu, mockEntityActionController, mockSynapseJSNIUtils, mockSynapseJavascriptClient, mockSynAlert, mockGWT, mockGinInjector);
		TableQueryResultWikiWidget widget2 = new TableQueryResultWikiWidget(mockView, mockActionMenu, mockEntityActionController, mockSynapseJSNIUtils, mockSynapseJavascriptClient, mockSynAlert, mockGWT, mockGinInjector);


		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TABLE_QUERY_KEY, "select * from syn12345");

		widget1.configure(wikiKey, descriptor, null, null);

		verifyZeroInteractions(mockGWT);
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(anyString(), asyncCallbackCaptor.capture());
		assertTrue(TableQueryResultWikiWidget.isLoading);

		widget2.configure(wikiKey, descriptor, null, null);

		verify(mockGWT).scheduleExecution(callbackCaptor.capture(), anyInt());
		// verify that it's still only attempted to get the entity bundle once
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(anyString(), any(AsyncCallback.class));

		// invoke the async callback to complete widget1 load, and verify that widget2 would begin to load
		// (on callback execution).
		asyncCallbackCaptor.getValue().onSuccess(mockEntityBundle);
		assertFalse(TableQueryResultWikiWidget.isLoading);

		callbackCaptor.getValue().invoke();

		assertTrue(TableQueryResultWikiWidget.isLoading);
		verify(mockSynapseJavascriptClient, times(2)).getEntityBundleFromCache(anyString(), any(AsyncCallback.class));
		// verify error of call also results in load complete
		asyncCallbackCaptor.getValue().onFailure(new Exception("failed to load second widget"));
		assertFalse(TableQueryResultWikiWidget.isLoading);
	}
}
