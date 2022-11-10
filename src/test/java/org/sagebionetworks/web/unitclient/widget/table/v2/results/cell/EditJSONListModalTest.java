package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParser;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EditJSONListModal;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EditJSONListModalView;

@RunWith(GwtMockitoTestRunner.class)
public class EditJSONListModalTest {

  @Mock
  EditJSONListModalView mockView;

  @Mock
  PortalGinInjector mockInjector;

  @Mock
  CellFactory mockCellFactory;

  @Mock
  Consumer<List<String>> mockOnSaveCallback;

  @Mock
  CellEditor mockCell1;

  @Mock
  CellEditor mockCell2;

  @Mock
  CellEditor mockCell3;

  @Mock
  GWTWrapper mockGWTWrapper;

  @Mock
  JSONArray mockJSONArray;

  @Mock
  CommaSeparatedValuesParser mockCommaSeparatedValuesParser;

  @InjectMocks
  EditJSONListModal modal;

  ColumnModel inputColumnModel;
  String jsonString;
  String listVal1;
  String listVal2;

  long maxListLength = 34L;
  long maxSize = 62L;

  @Before
  public void setup() {
    when(mockCellFactory.createEditor(any(ColumnModel.class)))
      .thenReturn(mockCell1, mockCell2, mockCell3);

    inputColumnModel = new ColumnModel();
    inputColumnModel.setColumnType(ColumnType.STRING_LIST);
    inputColumnModel.setMaximumSize(maxSize);
    inputColumnModel.setMaximumListLength(maxListLength);

    listVal1 = "abc";
    listVal2 = "def";
    jsonString = "[\"" + listVal1 + "\"," + listVal2 + "]";

    when(mockCell1.getValue()).thenReturn(listVal1);
    when(mockCell2.getValue()).thenReturn(listVal2);

    when(mockGWTWrapper.parseJSONStrict(any())).thenReturn(mockJSONArray);
    when(mockJSONArray.isArray()).thenReturn(mockJSONArray);
    when(mockJSONArray.size()).thenReturn(2);
    when(mockJSONArray.get(0)).thenReturn(new JSONString(listVal1));
    when(mockJSONArray.get(1)).thenReturn(new JSONString(listVal2));
  }

  @Test
  public void testConfigure_columnModelNullMaxSize() {
    inputColumnModel.setMaximumSize(null);

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    //assert default value used when null
    assertEquals(
      (Long) EditJSONListModal.DEFAULT_MAX_SIZE,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1).setValue(listVal1);
    verify(mockCell2).setValue(listVal2);
    verify(mockView, never()).showError(any());
    verify(mockView, times(2)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_columnModelNullMaxListLength() {
    inputColumnModel.setMaximumListLength(null);

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(
      EditJSONListModal.DEFAULT_MAX_LIST_LENGTH,
      modal.getMaxListLength()
    );

    verify(mockCell1).setValue(listVal1);
    verify(mockCell2).setValue(listVal2);
    verify(mockView, never()).showError(any());
    verify(mockView, times(2)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_jsonStringNull() {
    jsonString = null;

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1, never()).setValue(any());
    verify(mockCell2, never()).setValue(any());
    verify(mockView, never()).showError(any());
    //default empty editor added
    verify(mockView, times(1)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_jsonStringEmpty() {
    jsonString = "";

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1, never()).setValue(any());
    verify(mockCell2, never()).setValue(any());
    verify(mockView, never()).showError(any());
    //default empty editor added
    verify(mockView, times(1)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_notJSONArray() {
    jsonString = "{\"actually\":\"a dictonary\"}";
    when(mockGWTWrapper.parseJSONStrict(jsonString))
      .thenReturn(new JSONObject());

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1, never()).setValue(any());
    verify(mockCell2, never()).setValue(any());
    verify(mockView).showError(EditJSONListModal.NOT_A_VALID_JSON_ARRAY);
    //default empty editor added
    verify(mockView, times(1)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_invalidJSON() {
    jsonString = "[]][a]sf[asd]f[sda]f[][]af]]";
    when(mockGWTWrapper.parseJSONStrict(jsonString))
      .thenThrow(JSONException.class);

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1, never()).setValue(any());
    verify(mockCell2, never()).setValue(any());
    verify(mockView).showError(EditJSONListModal.NOT_A_VALID_JSON_ARRAY);
    //default empty editor added
    verify(mockView, times(1)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testConfigure_happy() {
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    verify(mockView).clearEditors();

    assertEquals(
      (Long) maxSize,
      modal.getEffectiveSingleValueColumnModel().getMaximumSize()
    );
    //assert default value used when null
    assertEquals(maxListLength, modal.getMaxListLength());

    verify(mockCell1).setValue(listVal1);
    verify(mockCell2).setValue(listVal2);
    verify(mockView, never()).showError(any());
    //values were added
    verify(mockView, times(2)).addNewEditor(any());
    verify(mockView).showEditor();
  }

  @Test
  public void testCreateNewEditor() {
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    CellEditor createdEditor = modal.createNewEditor();

    assertEquals(mockCell3, createdEditor);
    ArgumentCaptor<KeyDownHandler> captor = ArgumentCaptor.forClass(
      KeyDownHandler.class
    );
    verify(mockCell3).addKeyDownHandler(captor.capture());
    // test clicking ENTER adds a new editor
    KeyDownEvent mockEvent = mock(KeyDownEvent.class);
    when(mockEvent.getNativeKeyCode()).thenReturn(KeyCodes.KEY_ENTER);
    // addNewEditor() already called twice from configure()
    verify(mockView, times(2)).addNewEditor(any(CellEditor.class));
    captor.getValue().onKeyDown(mockEvent);
    verify(mockView, times(3)).addNewEditor(any(CellEditor.class));
    verify(mockCell3).setFocus(true);
  }

  @Test
  public void testOnSave_exceedMaxListLength() {
    inputColumnModel.setMaximumListLength(1L);
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    modal.onSave();

    verify(mockOnSaveCallback, never()).accept(any());
    verify(mockView)
      .showError(
        EditJSONListModal.EXCEEDED_MAXIMUM_NUMBER_OF_VALUES_DEFINED_IN_SCHEMA +
        inputColumnModel.getMaximumListLength()
      );
  }

  @Test
  public void testOnSave_someCellEditorInvalid() {
    when(mockCell1.isValid()).thenReturn(false);
    when(mockCell2.isValid()).thenReturn(true);
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    modal.onSave();

    verify(mockCell1).isValid();
    verify(mockCell2).isValid();
    verify(mockOnSaveCallback, never()).accept(any());
    verify(mockView).showError(EditJSONListModal.SEE_THE_ERRORS_ABOVE);
  }

  @Test
  public void testOnSave_happy() {
    when(mockCell1.isValid()).thenReturn(true);
    when(mockCell2.isValid()).thenReturn(true);

    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    modal.onSave();

    verify(mockCell1).isValid();
    verify(mockCell2).isValid();

    verify(mockOnSaveCallback).accept(Arrays.asList(listVal1, listVal2));

    verify(mockView, never()).showError(any());
  }

  @Test
  public void testOnClickPasteNewValues() {
    when(mockInjector.getCommaSeparatedValuesParser())
      .thenReturn(mockCommaSeparatedValuesParser);

    modal.onClickPasteNewValues();
    //on the first call, verify we created a new parser
    verify(mockInjector).getCommaSeparatedValuesParser();
    verify(mockCommaSeparatedValuesParser).configure(any());
    verify(mockView)
      .addCommaSeparatedValuesParser(mockCommaSeparatedValuesParser.asWidget());
    verify(mockCommaSeparatedValuesParser, never()).show();

    //call again to verify we never create another parser
    modal.onClickPasteNewValues();
    verify(mockCommaSeparatedValuesParser).show();

    //same verify() calls as before to ensure they were only called once
    verify(mockInjector).getCommaSeparatedValuesParser();
    verify(mockCommaSeparatedValuesParser).configure(any());
    verify(mockView)
      .addCommaSeparatedValuesParser(mockCommaSeparatedValuesParser.asWidget());
  }

  @Test
  public void testOnValueDeleted() {
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    modal.onValueDeleted(mockCell1);
    modal.onValueDeleted(mockCell2);

    //we add an empty editor in the cases all are deleted
    verify(mockView).addNewEditor(mockCell3);

    // in this case, all values were deleted so we don't move the new annotation value button
    verify(mockView, never()).moveAddNewAnnotationValueButtonToRowToLastRow();

    List<CellEditor> cellEditors = modal.getCellEditors();
    // an empty cell was added back
    assertEquals(1, cellEditors.size());
  }

  @Test
  public void testOnValueDeleted_lastValueDeleted() {
    modal.configure(jsonString, mockOnSaveCallback, inputColumnModel);

    modal.onValueDeleted(mockCell2);
    //we never added a new cell to replace all of the deleted cells
    verify(mockView, never()).addNewEditor(mockCell3);

    // in this case the second/last value was deleted so we move the add new row button
    verify(mockView).moveAddNewAnnotationValueButtonToRowToLastRow();

    List<CellEditor> cellEditors = modal.getCellEditors();
    assertEquals(1, cellEditors.size());
  }
}
