package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.RowSetUtils.ETAG_COLUMN_NAME;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ImportTableViewColumnsButton;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit test for ColumnModelsViewWidget
 *
 * @author jmhill
 *
 */
public class ColumnModelsEditorWidgetTest {

  AdapterFactory adapterFactory;

  @Mock
  ColumnModelsView mockEditor;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  CookieProvider mockCookies;

  @Mock
  ViewDefaultColumns mockFileViewDefaultColumns;

  ColumnModelsEditorWidget widget;
  List<ColumnModel> schema;

  @Mock
  ColumnModelTableRowEditorWidget mockColumnModelTableRowEditorWidget1;

  @Mock
  ColumnModelTableRowEditorWidget mockColumnModelTableRowEditorWidget2;

  @Mock
  ImportTableViewColumnsButton mockAddTableViewColumnsButton;

  @Captor
  ArgumentCaptor<List<ColumnModelTableRow>> columnModelTableRowsCaptor;

  ColumnModel nonEditableColumn;
  List<ColumnModel> nonEditableColumns;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    adapterFactory = new AdapterFactoryImpl();
    when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockEditor);
    when(mockGinInjector.createColumnModelEditorWidget())
      .thenAnswer(
        new Answer<ColumnModelTableRowEditorWidget>() {
          @Override
          public ColumnModelTableRowEditorWidget answer(
            InvocationOnMock invocation
          ) throws Throwable {
            return new ColumnModelTableRowEditorStub();
          }
        }
      );
    when(mockGinInjector.createNewColumnModelTableRowViewer())
      .thenAnswer(
        new Answer<ColumnModelTableRowViewer>() {
          @Override
          public ColumnModelTableRowViewer answer(InvocationOnMock invocation)
            throws Throwable {
            return new ColumnModelTableRowViewerStub();
          }
        }
      );
    when(mockGinInjector.getImportTableViewColumnsButton())
      .thenReturn(mockAddTableViewColumnsButton);
    when(mockGinInjector.getCookieProvider()).thenReturn(mockCookies);
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn("true");
    nonEditableColumns = new ArrayList<ColumnModel>();
    nonEditableColumn = new ColumnModel();
    nonEditableColumn.setColumnType(ColumnType.STRING);
    String colName1 = "non-editable default column";
    nonEditableColumn.setName(colName1);
    nonEditableColumns.add(nonEditableColumn);

    nonEditableColumn = new ColumnModel();
    nonEditableColumn.setColumnType(ColumnType.STRING);
    nonEditableColumn.setName(ETAG_COLUMN_NAME);
    nonEditableColumns.add(nonEditableColumn);
    Set<String> nonEditableColumnNames = new HashSet<String>();
    nonEditableColumnNames.add(colName1);
    nonEditableColumnNames.add(ETAG_COLUMN_NAME);

    when(
      mockFileViewDefaultColumns.getDefaultViewColumnNames(any(TableType.class))
    )
      .thenReturn(nonEditableColumnNames);
    widget =
      new ColumnModelsEditorWidget(
        mockGinInjector,
        adapterFactory,
        mockFileViewDefaultColumns
      );
    schema = TableModelTestUtils.createOneOfEachType(true);
  }

  @Test
  public void testConfigure() {
    widget.configure(TableType.table, schema);
    verify(mockEditor).configure(ViewType.EDITOR, true);
    // All rows should be added to the editor
    verify(mockEditor).addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
    verify(mockGinInjector, times(schema.size()))
      .createColumnModelEditorWidget();
    // Extract the columns from the editor
    List<ColumnModel> clone = widget.getEditedColumnModels();
    assertEquals(schema, clone);

    // note that attempting to add all columns again is a no-op if they all exist in the editor already
    // select all, delete, and add schema again (verify all columns from the schema have been re-added).
    widget.selectAll();
    widget.deleteSelected();
    widget.addColumns(schema);
    verify(mockEditor, times(2))
      .addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(
      schema.size(),
      columnModelTableRowsCaptor.getAllValues().get(0).size()
    );
    assertEquals(
      schema.size(),
      columnModelTableRowsCaptor.getAllValues().get(1).size()
    );
  }

  @Test
  public void testAddDuplicateColumns() {
    widget.configure(TableType.table, schema);

    List<ColumnModel> clone = widget.getEditedColumnModels();
    assertEquals(schema, clone);

    // change the column type of the first column
    clone.get(0).setId(null);
    clone.get(0).setColumnType(ColumnType.FILEHANDLEID);
    // change the column name of the second column
    clone.get(1).setId(null);
    clone.get(1).setName("newname");

    // the third column looks to be new, but should be filtered out because it has the same name and
    // type as an existing column in the editor
    clone.get(2).setId(null);
    // all other columns should be ignored (because they have the same name and type)
    widget.addColumns(clone);
    verify(mockEditor, times(2))
      .addColumns(columnModelTableRowsCaptor.capture());
    assertEquals(
      schema.size(),
      columnModelTableRowsCaptor.getAllValues().get(0).size()
    );
    assertEquals(2, columnModelTableRowsCaptor.getAllValues().get(1).size());
  }

  @Test
  public void testFileView() {
    // try to add non-editable column
    when(mockGinInjector.createColumnModelEditorWidget())
      .thenReturn(
        mockColumnModelTableRowEditorWidget1,
        mockColumnModelTableRowEditorWidget2
      );
    widget.configure(TableType.file_view, nonEditableColumns);
    verify(mockGinInjector, times(nonEditableColumns.size()))
      .createColumnModelEditorWidget();
    verify(mockColumnModelTableRowEditorWidget1).setToBeDefaultFileViewColumn();
    verify(mockColumnModelTableRowEditorWidget2).setToBeDefaultFileViewColumn();
    verify(mockColumnModelTableRowEditorWidget1).setIsView();
    verify(mockColumnModelTableRowEditorWidget2).setIsView();
  }

  @Test
  public void testAddNewColumn() {
    widget.configure(TableType.table, schema);
    // This should add a new string column
    widget.addNewColumn();
    // A string should be added...
    ColumnModel newModel = new ColumnModel();
    newModel.setColumnType(ColumnModelsEditorWidget.DEFAULT_NEW_COLUMN_TYPE);
    newModel.setMaximumSize(ColumnModelsEditorWidget.DEFAULT_STRING_MAX_SIZE);
    schema.add(newModel);
    // Extract the columns from the editor
    List<ColumnModel> clone = widget.getEditedColumnModels();
    assertEquals(schema, clone);
  }

  @Test
  public void testOnSaveSuccessValidateFalse()
    throws JSONObjectAdapterException {
    widget.configure(TableType.table, schema);
    // Add a column
    ColumnModelTableRowEditorStub editor = (ColumnModelTableRowEditorStub) widget.addNewColumn();
    editor.setValid(false);
    editor.setColumnName("a name");
    // Now call validate
    assertFalse(widget.validate());
  }

  @Test
  public void testSelectAll() {
    widget.configure(TableType.table, schema);
    // checks selection state after all column editors have been added.
    verify(mockEditor).setCanDelete(false);
    verify(mockEditor).setCanMoveUp(false);
    verify(mockEditor).setCanMoveDown(false);

    // Add three columns
    reset(mockEditor);
    ColumnModelTableRowEditorWidget one = widget.addNewColumn();
    verify(mockEditor).setCanDelete(false);
    verify(mockEditor).setCanMoveUp(false);
    verify(mockEditor).setCanMoveDown(false);
    assertTrue(((ColumnModelTableRowEditorStub) one).canHaveDefault());

    ColumnModelTableRowEditorWidget two = widget.addNewColumn();
    // Start with two selected
    reset(mockEditor);
    two.setSelected(true);
    verify(mockEditor).setCanDelete(true);
    verify(mockEditor).setCanMoveUp(true);
    verify(mockEditor).setCanMoveDown(false);

    reset(mockEditor);
    ColumnModelTableRowEditorWidget three = widget.addNewColumn();
    // With a new row the second row can move down.
    verify(mockEditor).setCanDelete(true);
    verify(mockEditor).setCanMoveUp(true);
    verify(mockEditor).setCanMoveDown(true);
    // select all
    reset(mockEditor);

    widget.selectAll();
    assertTrue(one.isSelected());
    assertTrue(two.isSelected());
    assertTrue(three.isSelected());
    // The select all must not attempt to change the state
    // of the buttons for each selection and instead
    // update the state at the end of the selection.
    verify(mockEditor).setCanDelete(true);
    verify(mockEditor).setCanMoveUp(false);
    verify(mockEditor).setCanMoveDown(false);
  }

  @Test
  public void testSelectNone() {
    widget.configure(TableType.table, schema);
    // Add three columns
    ColumnModelTableRowEditorWidget one = widget.addNewColumn();
    ColumnModelTableRowEditorWidget two = widget.addNewColumn();
    // Start with two selected
    two.setSelected(true);
    ColumnModelTableRowEditorWidget three = widget.addNewColumn();
    // select all
    widget.selectNone();
    assertFalse(one.isSelected());
    assertFalse(two.isSelected());
    assertFalse(three.isSelected());
  }

  @Test
  public void testToggleSelect() {
    widget.configure(TableType.table, schema);
    // Add three columns
    ColumnModelTableRowEditorWidget one = widget.addNewColumn();
    ColumnModelTableRowEditorWidget two = widget.addNewColumn();
    // Start with two selected
    two.setSelected(true);
    ColumnModelTableRowEditorWidget three = widget.addNewColumn();
    // select all
    widget.toggleSelect();
    assertFalse(one.isSelected());
    assertFalse(two.isSelected());
    assertFalse(three.isSelected());
    // do it again
    widget.toggleSelect();
    assertTrue(one.isSelected());
    assertTrue(two.isSelected());
    assertTrue(three.isSelected());
  }
}
