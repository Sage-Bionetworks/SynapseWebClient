package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.google.gwt.event.shared.EventBus;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.*;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.v2.schema.*;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit test for ColumnModelsViewWidget
 *
 * @author jmhill
 *
 */
public class ColumnModelsWidgetTest {

  @Mock
  ColumnModelsView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  ColumnModelsWidget widget;

  @Mock
  EntityBundle mockBundle;

  @Mock
  EntityView mockEntityView;

  @Mock
  SubmissionView mockSubmissionView;

  @Mock
  List<String> mockViewScopeIds;

  @Mock
  EventBus mockEventBus;

  @Mock
  PopupUtilsView mockPopupUtilsView;

  @Captor
  ArgumentCaptor<List<ColumnModelTableRow>> columnModelTableRowsCaptor;

  @Mock
  ColumnModelsEditorWidget mockEditor;

  @Captor
  ArgumentCaptor<String> entityIdCaptor;

  @Captor
  ArgumentCaptor<
    TableColumnSchemaEditorProps.OnColumnsUpdated
  > onColumnsUpdatedCaptor;

  @Captor
  ArgumentCaptor<TableColumnSchemaEditorProps.OnCancel> onCancelCaptor;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  public static final String NEXT_PAGE_TOKEN = "nextPageToken";

  TableEntity table;
  TableBundle tableBundle;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    table = new TableEntity();
    table.setId("syn123");
    tableBundle = new TableBundle();
    when(mockBundle.getEntity()).thenReturn(table);
    when(mockBundle.getTableBundle()).thenReturn(tableBundle);
    when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockView);
    when(mockGinInjector.createColumnModelEditorWidget())
      .thenAnswer(
        (Answer<ColumnModelTableRowEditorWidget>) invocation ->
          new ColumnModelTableRowEditorStub()
      );
    when(mockGinInjector.createNewColumnModelTableRowViewer())
      .thenAnswer(
        (Answer<ColumnModelTableRowViewer>) invocation ->
          new ColumnModelTableRowViewerStub()
      );
    widget =
      new ColumnModelsWidget(mockGinInjector, mockPopupUtilsView, mockEditor);

    when(mockEntityView.getScopeIds()).thenReturn(mockViewScopeIds);
    when(mockEntityView.getType())
      .thenReturn(org.sagebionetworks.repo.model.table.ViewType.file);
    when(mockEntityView.getViewTypeMask()).thenReturn(null);
    when(mockGinInjector.getEventBus()).thenReturn(mockEventBus);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setEditHandler(widget);
    verify(mockView).setEditor(mockEditor);
  }

  @Test
  public void testConfigure() {
    boolean isEditable = true;
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    verify(mockView).configure(isEditable);
    // All rows should be added to both the viewer and editor
    verify(mockView).addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
  }

  @Test
  public void testConfigureView() {
    boolean isEditable = true;
    when(mockBundle.getEntity()).thenReturn(mockEntityView);
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    verify(mockView).configure(isEditable);
    // All rows should be added to both the viewer and editor
    verify(mockView).addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
  }

  @Test
  public void testConfigureSubmissionView() {
    boolean isEditable = true;
    when(mockBundle.getEntity()).thenReturn(mockSubmissionView);
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    verify(mockView).configure(isEditable);
    // All rows should be added to both the viewer and editor
    verify(mockView).addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
  }

  @Test
  public void testOnEditColumnsSuccess() {
    boolean isEditable = true;
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    // show the editor
    widget.onEditColumns();

    verify(mockEditor)
      .configure(
        entityIdCaptor.capture(),
        onColumnsUpdatedCaptor.capture(),
        onCancelCaptor.capture()
      );

    assertEquals(table.getId(), entityIdCaptor.getValue());

    // simulate a successful update
    onColumnsUpdatedCaptor.getValue().onColumnsUpdated();
    verify(mockPopupUtilsView).notify(anyString(), anyString(), any());
    verify(mockEditor).setOpen(false);
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnEditColumnsCancelled() {
    boolean isEditable = true;
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    // show the editor
    widget.onEditColumns();

    verify(mockEditor)
      .configure(
        entityIdCaptor.capture(),
        onColumnsUpdatedCaptor.capture(),
        onCancelCaptor.capture()
      );

    assertEquals(table.getId(), entityIdCaptor.getValue());

    // simulate cancelling the update modal
    onCancelCaptor.getValue().onCancel();
    verify(mockPopupUtilsView)
      .showConfirmDialog(anyString(), anyString(), callbackCaptor.capture());

    // Confirm the popup
    callbackCaptor.getValue().invoke();

    verify(mockEditor).setOpen(false);
    verifyZeroInteractions(mockEventBus);
  }

  @Test(expected = IllegalStateException.class)
  public void testOnEditNonEditable() {
    boolean isEditable = false;
    List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
    tableBundle.setColumnModels(schema);
    widget.configure(mockBundle, isEditable);
    // should fail
    widget.onEditColumns();
  }
}
