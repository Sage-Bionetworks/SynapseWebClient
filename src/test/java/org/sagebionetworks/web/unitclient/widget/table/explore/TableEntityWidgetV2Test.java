package org.sagebionetworks.web.unitclient.widget.table.explore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2.HIDE;
import static org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2.SCHEMA;
import static org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2.SCOPE;
import static org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2.SHOW;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityRef;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnQueryResultBundleCallback;
import org.sagebionetworks.web.client.jsinterop.QueryWrapperPlotNavProps.OnViewSharingSettingsHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.clienthelp.FileViewClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v3.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v3.ActionListener;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.explore.TableEntityWidgetV2;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadTableQueryModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadTableModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Business logic tests for the TableEntityPlotsWidget
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TableEntityWidgetV2Test {

  AdapterFactory adapterFactory;
  List<ColumnModel> columns;
  TableBundle tableBundle;
  TableEntity tableEntity;

  @Mock
  EntityActionMenu mockActionMenu;

  @Mock
  DownloadTableQueryModalWidget mockDownloadTableQueryModalWidget;

  @Mock
  UploadTableModalWidget mockUploadTableModalWidget;

  @Mock
  PreflightController mockPreflightController;

  @Mock
  TableEntityWidgetView mockView;

  @Mock
  QueryChangeHandler mockQueryChangeHandler;

  TableEntityWidgetV2 widget;
  EntityBundle entityBundle;
  Long versionNumber;

  @Mock
  SynapseClientAsync mockSynapseClient;

  @Mock
  CopyTextModal mockCopyTextModal;

  @Mock
  CookieProvider mockCookies;

  @Mock
  EventBus mockEventBus;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  AccessControlListModalWidget mockACLModalWidget;

  @Mock
  Entity mockEntity;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Captor
  ArgumentCaptor<Query> queryCaptor;

  public static final String FACET_SQL =
    "select * from syn123 where \"x\" = 'a'";
  public static final String EXPECTED_SQL_FOR_CLIENT =
    "select * from syn123 where \\\"x\\\" = 'a'";

  @Mock
  FileViewClientsHelp mockFileViewClientsHelp;

  @Mock
  PortalGinInjector mockPortalGinInjector;

  @Mock
  AddToDownloadListV2 mockAddToDownloadListV2;

  @Mock
  SessionStorage mockSessionStorage;

  @Mock
  AuthenticationController mockAuthController;

  @Captor
  ArgumentCaptor<ActionListener> actionListenerCaptor;

  @Mock
  TotalVisibleResultsWidget mockTotalVisibleResultsWidget;

  @Mock
  QueryResultEditorWidget mockQueryResultEditorWidget;

  @Captor
  ArgumentCaptor<OnQueryCallback> onQueryCallbackCaptor;

  @Captor
  ArgumentCaptor<OnQueryResultBundleCallback> onQueryResultBundleCallbackCaptor;

  @Captor
  ArgumentCaptor<OnViewSharingSettingsHandler> onViewSharingSettingsHandlerCaptor;

  @Mock
  GlobalApplicationState mockGlobalState;

  JSONObjectAdapterImpl portalJson = new JSONObjectAdapterImpl();

  @Before
  public void before() {
    // stubs
    adapterFactory = new AdapterFactoryImpl();
    columns = TableModelTestUtils.createOneOfEachType();
    tableEntity = new TableEntity();
    tableEntity.setId("syn123");
    tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
    tableBundle = new TableBundle();
    tableBundle.setMaxRowsPerPage(4L);
    tableBundle.setColumnModels(columns);
    versionNumber = null;
    when(mockPortalGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalState);
    when(mockPortalGinInjector.getDownloadTableQueryModalWidget())
      .thenReturn(mockDownloadTableQueryModalWidget);
    when(mockPortalGinInjector.getUploadTableModalWidget())
      .thenReturn(mockUploadTableModalWidget);
    when(mockPortalGinInjector.getCopyTextModal())
      .thenReturn(mockCopyTextModal);
    when(mockPortalGinInjector.getAuthenticationController())
      .thenReturn(mockAuthController);
    when(mockPortalGinInjector.getCookieProvider()).thenReturn(mockCookies);
    when(mockPortalGinInjector.getAddToDownloadListV2())
      .thenReturn(mockAddToDownloadListV2);
    when(mockPortalGinInjector.createNewQueryResultEditorWidget())
      .thenReturn(mockQueryResultEditorWidget);
    when(mockPortalGinInjector.getJSONObjectAdapter()).thenReturn(portalJson);
    when(mockPortalGinInjector.getAccessControlListModalWidget())
      .thenReturn(mockACLModalWidget);
    when(mockPortalGinInjector.getSynapseJavascriptClient())
      .thenReturn(mockJsClient);
    AsyncMockStubber
      .callSuccessWith(mockEntity)
      .when(mockJsClient)
      .getEntity(anyString(), any(AsyncCallback.class));
    widget =
      new TableEntityWidgetV2(
        mockView,
        mockPreflightController,
        mockSynapseClient,
        mockFileViewClientsHelp,
        mockPortalGinInjector,
        mockSessionStorage,
        mockEventBus
      );

    AsyncMockStubber
      .callSuccessWith(FACET_SQL)
      .when(mockSynapseClient)
      .generateSqlWithFacets(
        anyString(),
        anyList(),
        anyList(),
        any(AsyncCallback.class)
      );
    // The test bundle
    entityBundle = new EntityBundle();
    entityBundle.setEntity(tableEntity);
    entityBundle.setTableBundle(tableBundle);

    String sql = "SELECT * FROM " + tableEntity.getId() + " LIMIT 3 OFFSET 0";
    Query query = new Query();
    query.setSql(sql);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
    Header.isShowingPortalAlert = false;
    Header.portalAlertJson = null;
  }

  private void configureBundleWithView(ViewType viewType) {
    EntityView view = new EntityView();
    view.setId("syn456");
    view.setScopeIds(Collections.singletonList("syn789"));
    view.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
    view.setType(viewType);
    entityBundle.setEntity(view);
  }

  private void configureBundleWithDataset() {
    Dataset dataset = new Dataset();
    dataset.setId("syn456");
    dataset.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));

    // Dataset has one item
    EntityRef item = new EntityRef();
    item.setEntityId("syn123");
    item.setVersionNumber(1L);
    dataset.setItems(Collections.singletonList(item));

    entityBundle.setEntity(dataset);
  }

  private void configureBundleWithDatasetCollection() {
    DatasetCollection datasetCollection = new DatasetCollection();
    datasetCollection.setId("syn456");
    datasetCollection.setColumnIds(
      TableModelTestUtils.getColumnModelIds(columns)
    );

    // Dataset has one item
    EntityRef item = new EntityRef();
    item.setEntityId("syn123");
    item.setVersionNumber(1L);
    datasetCollection.setItems(Collections.singletonList(item));

    entityBundle.setEntity(datasetCollection);
  }

  @Test
  public void testGetDefaultPageSizeMaxUnder() {
    tableBundle.setMaxRowsPerPage(4L);
    // Configure with the default values
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    // since the size from the bundle is less than the default,
    // the value used should be 3/4ths of the max allowed for the schema.
    assertEquals(3l, widget.getDefaultPageSize());
  }

  @Test
  public void testGetDefaultPageSizeMaxOver() {
    tableBundle.setMaxRowsPerPage(QueryBundleUtils.DEFAULT_LIMIT * 2L);
    // Configure with the default values
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    // since the size from the bundle is greater than the default
    // the default should be used.
    assertEquals(QueryBundleUtils.DEFAULT_LIMIT, widget.getDefaultPageSize());
  }

  @Test
  public void testGetDefaultPageSizeNull() {
    tableBundle.setMaxRowsPerPage(null);
    // Configure with the default values
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    // when null the default should be used.
    assertEquals(QueryBundleUtils.DEFAULT_LIMIT, widget.getDefaultPageSize());
  }

  @Test
  public void testConfigureNotNullDefaultQuery() {
    tableBundle.setMaxRowsPerPage(4L);
    // This time we pass a query
    String sql = "SELECT * FROM " + tableEntity.getId() + " LIMIT 3 OFFSET 0";
    Query query = new Query();
    query.setSql(sql);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(query);
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    // The widget must not change the query when it is passed in.
    verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
  }

  @Test
  public void testNoColumnsWithEdit() {
    entityBundle
      .getTableBundle()
      .setColumnModels(new LinkedList<ColumnModel>());
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView).setTableMessageVisible(true);
    verify(mockView)
      .showTableMessage(
        AlertType.INFO,
        TableEntityWidgetV2.getNoColumnsMessage(TableType.table, canEdit)
      );
    // The query should be cleared when there are no columns
    verify(mockQueryChangeHandler).onQueryChange(null);
  }

  @Test
  public void testNoColumnsWithWihtouEdit() {
    entityBundle
      .getTableBundle()
      .setColumnModels(new LinkedList<ColumnModel>());
    boolean canEdit = false;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView).setTableMessageVisible(true);
    verify(mockView)
      .showTableMessage(
        AlertType.INFO,
        TableEntityWidgetV2.getNoColumnsMessage(TableType.table, canEdit)
      );
    // The query should be cleared when there are no columns
    verify(mockQueryChangeHandler).onQueryChange(null);
  }

  @Test
  public void testQueryExecutionStarted() {
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.queryExecutionStarted();
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, false);
  }

  @Test
  public void testQueryExecutionFinishedSuccess() {
    boolean canEdit = true;
    boolean wasExecutionSuccess = true;
    boolean resultsEditable = true;
    Query startQuery = new Query();
    startQuery.setSql("select * from syn123");
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    reset(mockActionMenu);
    widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
    verify(mockQueryChangeHandler).onQueryChange(startQuery);
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
  }

  @Test
  public void testViewQueryExecutionFinishedSuccess() {
    configureBundleWithView(ViewType.file);
    boolean canEdit = true;
    boolean wasExecutionSuccess = true;
    boolean resultsEditable = true;
    Query startQuery = new Query();
    startQuery.setSql("select * from syn123");
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    reset(mockActionMenu);
    widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
    verify(mockQueryChangeHandler).onQueryChange(startQuery);
    verify(mockActionMenu).setActionVisible(Action.EDIT_TABLE_DATA, true);
  }

  @Test
  public void testQueryExecutionFinishedSuccessNoEdit() {
    boolean canEdit = false;
    boolean wasExecutionSuccess = true;
    boolean resultsEditable = true;
    Query startQuery = new Query();
    startQuery.setSql("select * from syn123");
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    reset(mockActionMenu);
    widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
    verify(mockQueryChangeHandler).onQueryChange(startQuery);
    verify(mockActionMenu, never())
      .setActionVisible(Action.EDIT_TABLE_DATA, true);
  }

  @Test
  public void testQueryExecutionFinishedFailed() {
    boolean canEdit = true;
    boolean wasExecutionSuccess = false;
    boolean resultsEditable = true;
    Query startQuery = new Query();
    startQuery.setSql("select * from syn123");
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    reset(mockActionMenu);
    widget.queryExecutionFinished(wasExecutionSuccess, resultsEditable);
    verify(mockQueryChangeHandler, never()).onQueryChange(any(Query.class));
    verify(mockActionMenu, never())
      .setActionVisible(Action.EDIT_TABLE_DATA, true);
  }

  @Test
  public void testOnExecuteQuery() throws JSONObjectAdapterException {
    boolean canEdit = true;
    // Start with a query that is not on the first page
    Query startQuery = new Query();
    String startingSql = "select * from syn123";
    startQuery.setSql(startingSql);
    startQuery.setLimit(100L);
    startQuery.setOffset(101L);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    boolean expectedHideSqlEditorControl = false;
    // Start query get passed to the results
    JSONObjectAdapterImpl adapter = new JSONObjectAdapterImpl();
    startQuery.writeToJSONObject(adapter);

    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView)
      .configureQueryWrapperPlotNav(
        eq(startingSql),
        eq(adapter.toJSONString()),
        onQueryCallbackCaptor.capture(),
        onQueryResultBundleCallbackCaptor.capture(),
        onViewSharingSettingsHandlerCaptor.capture(),
        eq(expectedHideSqlEditorControl)
      );

    // test onQueryCallbackCaptor functionality.
    // if onQueryCallbackCaptor is called, then the current query is updated
    assertEquals(startingSql, widget.getCurrentQuery().getSql());
    String newSql = "select this from that";
    startQuery.setSql(newSql);
    startQuery.writeToJSONObject(adapter);
    String newQueryJson = adapter.toJSONString();

    OnQueryCallback queryChangedCallback = onQueryCallbackCaptor.getValue();
    queryChangedCallback.run(newQueryJson);

    assertEquals(newSql, widget.getCurrentQuery().getSql());

    // test onQueryResultBundleCallbackCaptor functionality.
    // if onQueryResultBundleCallbackCaptor is called, then the current result
    // bundle is updated
    assertNull(widget.getCurrentQueryResultBundle());
    QueryResultBundle newBundle = new QueryResultBundle();
    Long expectedQueryCount = 125L;
    newBundle.setQueryCount(expectedQueryCount);
    newBundle.writeToJSONObject(adapter);

    OnQueryResultBundleCallback queryResultCallback = onQueryResultBundleCallbackCaptor.getValue();
    queryResultCallback.run(adapter.toJSONString());

    assertEquals(
      expectedQueryCount,
      widget.getCurrentQueryResultBundle().getQueryCount()
    );

    // test OnViewSharingSettingsHandler
    OnViewSharingSettingsHandler onViewSharingSettingsHandler = onViewSharingSettingsHandlerCaptor.getValue();
    String testEntityId = "syn0000001";
    onViewSharingSettingsHandler.onViewSharingSettingsClicked(testEntityId);

    verify(mockJsClient).getEntity(eq(testEntityId), any(AsyncCallback.class));
    verify(mockACLModalWidget).configure(mockEntity, false);
    verify(mockACLModalWidget).showSharing(any(Callback.class));
  }

  @Test
  public void testShowSchema() {
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.SHOW_TABLE_SCHEMA),
        actionListenerCaptor.capture()
      );
    ActionListener listener = actionListenerCaptor.getValue();
    verify(mockView).setSchemaVisible(false);
    listener.onAction(Action.SHOW_TABLE_SCHEMA, null);
    verify(mockView).setSchemaVisible(true);
    verify(mockActionMenu)
      .setActionText(Action.SHOW_TABLE_SCHEMA, HIDE + "Table" + SCHEMA);
    listener.onAction(Action.SHOW_TABLE_SCHEMA, null);
    verify(mockView).setSchemaVisible(false);
    verify(mockActionMenu)
      .setActionText(Action.SHOW_TABLE_SCHEMA, SHOW + "Table" + SCHEMA);
  }

  @Test
  public void testShowViewScope() {
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.SHOW_VIEW_SCOPE),
        actionListenerCaptor.capture()
      );
    ActionListener listener = actionListenerCaptor.getValue();
    verify(mockView).setScopeVisible(false);
    listener.onAction(Action.SHOW_VIEW_SCOPE, null);
    verify(mockView).setScopeVisible(true);
    verify(mockActionMenu)
      .setActionText(Action.SHOW_VIEW_SCOPE, HIDE + SCOPE + "Table");
    listener.onAction(Action.SHOW_VIEW_SCOPE, null);
    verify(mockView).setScopeVisible(false);
    verify(mockActionMenu)
      .setActionText(Action.SHOW_VIEW_SCOPE, SHOW + SCOPE + "Table");
  }

  @Test
  public void testUploadTableCSVPreflightFailed() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.onUploadTableData();
    // should not proceed to upload.
    verify(mockUploadTableModalWidget, never())
      .showModal(any(WizardCallback.class));
  }

  @Test
  public void testUploadTableCSVPreflightPassed() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.onUploadTableData();
    // proceed to upload
    verify(mockUploadTableModalWidget).showModal(any(WizardCallback.class));
  }

  @Test
  public void testEditTablePreflightFailed() {
    AsyncMockStubber
      .callNoInvovke()
      .when(mockPreflightController)
      .checkUpdateEntity(any(EntityBundle.class), any(Callback.class));
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.onEditResults();
    // should not proceed to edit
    verify(mockQueryResultEditorWidget, never())
      .showEditor(any(QueryResultBundle.class), any(TableType.class));
  }

  @Test
  public void testEditTablePreflightPassed() {
    AsyncMockStubber
      .callWithInvoke()
      .when(mockPreflightController)
      .checkUploadToEntity(any(EntityBundle.class), any(Callback.class));
    boolean canEdit = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.onEditResults();
    // proceed to edit
    verify(mockQueryResultEditorWidget)
      .showEditor(any(QueryResultBundle.class), any(TableType.class));
  }

  @Test
  public void testOnShowDownloadFiles() {
    Query startQuery = new Query();
    startQuery.setSql(FACET_SQL);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(startQuery);
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    widget.onShowDownloadFilesProgrammatically();

    verify(mockFileViewClientsHelp).setQuery(EXPECTED_SQL_FOR_CLIENT);
    verify(mockFileViewClientsHelp).show();
  }

  @Test
  public void testAutoAddToDownloadListV2() throws JSONObjectAdapterException {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    configureBundleWithView(ViewType.file);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
    Header.isShowingPortalAlert = true;
    Header.portalAlertJson = portalJson;
    portalJson.put(TableEntityWidgetV2.IS_INVOKING_DOWNLOAD_TABLE, true);

    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.queryExecutionFinished(true, false);

    verify(mockAddToDownloadListV2).configure(anyString(), any(Query.class));
    assertFalse(
      portalJson.getBoolean(TableEntityWidgetV2.IS_INVOKING_DOWNLOAD_TABLE)
    );
  }

  @Test
  public void testAutoAddToDownloadListFalse()
    throws JSONObjectAdapterException {
    when(mockAuthController.isLoggedIn()).thenReturn(true);
    configureBundleWithView(ViewType.file);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
    Header.isShowingPortalAlert = true;
    Header.portalAlertJson = portalJson;
    portalJson.put(TableEntityWidgetV2.IS_INVOKING_DOWNLOAD_TABLE, false);

    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.queryExecutionFinished(true, false);

    verify(mockAddToDownloadListV2, never())
      .configure(anyString(), any(Query.class));
  }

  @Test
  public void testAutoAddToDownloadListNotLoggedIn()
    throws JSONObjectAdapterException {
    when(mockAuthController.isLoggedIn()).thenReturn(false);
    configureBundleWithView(ViewType.file);
    when(mockQueryChangeHandler.getQueryString()).thenReturn(new Query());
    Header.isShowingPortalAlert = true;
    Header.portalAlertJson = portalJson;
    portalJson.put(TableEntityWidgetV2.IS_INVOKING_DOWNLOAD_TABLE, true);

    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    widget.queryExecutionFinished(true, false);

    verify(mockAddToDownloadListV2, never())
      .configure(anyString(), any(Query.class));
  }

  @Test
  public void testShowAndHideDatasetEditor() throws JSONObjectAdapterException {
    boolean canEdit = true;
    configureBundleWithDataset();
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.EDIT_ENTITYREF_COLLECTION_ITEMS),
        actionListenerCaptor.capture()
      );
    ActionListener listener = actionListenerCaptor.getValue();

    listener.onAction(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, null);
    verify(mockView).setItemsEditorVisible(true);

    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);

    // The React component will trigger the call to closeItemsEditor when we are
    // ready to close
    widget.closeItemsEditor();
    verify(mockView).setItemsEditorVisible(false);
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, true);
  }

  @Test // SWC-5921
  public void testShowDatasetEditorIfNoItemsAndEditable() {
    // The editor should be revealed without user interaction if the user can edit
    // the dataset AND it has no items.
    boolean canEdit = true;
    configureBundleWithDataset();

    ((Dataset) entityBundle.getEntity()).setItems(Collections.emptyList());

    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView).setItemsEditorVisible(true);
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test // SWC-5921
  public void testDoNotShowDatasetEditorIfHasItems() {
    boolean canEdit = true;
    configureBundleWithDataset();

    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView, never()).setItemsEditorVisible(true);
    verify(mockActionMenu, never())
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test // SWC-5921
  public void testDoNotShowDatasetEditorIfNotEditable() {
    boolean canEdit = false; // user does not have permission to edit
    configureBundleWithDataset();

    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView, never()).setItemsEditorVisible(true);
    verify(mockActionMenu, never())
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);
  }

  @Test
  public void testTableOnly() {
    boolean canEdit = true;
    boolean showTableOnly = true;
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      showTableOnly,
      mockQueryChangeHandler,
      mockActionMenu
    );

    verify(mockView).configureTableOnly(anyString());
  }

  @Test
  public void testToggleSchemaCollapse() {
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    // The handler has already been configured, just verify that it gets invoked
    widget.toggleSchemaCollapse();
    verify(mockActionMenu).onAction(Action.SHOW_TABLE_SCHEMA, null);
  }

  @Test
  public void testToggleScopeCollapse() {
    widget.configure(
      entityBundle,
      versionNumber,
      true,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );

    // The handler has already been configured, just verify that it gets invoked
    widget.toggleScopeCollapse();
    verify(mockActionMenu).onAction(Action.SHOW_VIEW_SCOPE, null);
  }

  @Test
  public void testHideQueryDataOnDatasetEdit() {
    boolean canEdit = true;
    configureBundleWithDataset();

    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.EDIT_ENTITYREF_COLLECTION_ITEMS),
        actionListenerCaptor.capture()
      );

    ActionListener onEditDatasetItems = actionListenerCaptor.getValue();

    // Check that we begin with the items editor closed.
    verify(mockView, never()).setItemsEditorVisible(true);
    verify(mockView, times(2)).setQueryWrapperPlotNavVisible(true);

    // Call under test - open the Dataset Items editor
    onEditDatasetItems.onAction(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, null);
    verify(mockView).setItemsEditorVisible(true);
    verify(mockView).setQueryWrapperPlotNavVisible(false);

    // Call under test - close the editor
    widget.closeItemsEditor();
    verify(mockView).setItemsEditorVisible(false);
    verify(mockView, times(4)).setQueryWrapperPlotNavVisible(true);
  }

  @Test
  public void testShowAndHideDatasetCollectionEditor()
    throws JSONObjectAdapterException {
    boolean canEdit = true;
    configureBundleWithDatasetCollection();
    widget.configure(
      entityBundle,
      versionNumber,
      canEdit,
      false,
      mockQueryChangeHandler,
      mockActionMenu
    );
    verify(mockActionMenu)
      .setActionListener(
        eq(Action.EDIT_ENTITYREF_COLLECTION_ITEMS),
        actionListenerCaptor.capture()
      );
    ActionListener listener = actionListenerCaptor.getValue();

    listener.onAction(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, null);
    verify(mockView).setItemsEditorVisible(true);

    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, false);

    // The React component will trigger the call to closeItemsEditor when we are
    // ready to close
    widget.closeItemsEditor();
    verify(mockView).setItemsEditorVisible(false);
    verify(mockActionMenu)
      .setActionVisible(Action.EDIT_ENTITYREF_COLLECTION_ITEMS, true);
  }
}
