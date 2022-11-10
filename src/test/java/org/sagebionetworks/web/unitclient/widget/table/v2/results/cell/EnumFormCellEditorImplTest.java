package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumFormCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.RadioCellEditorView;

@RunWith(MockitoJUnitRunner.class)
public class EnumFormCellEditorImplTest {

  @Mock
  DivView mockDivView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  ListCellEditorView mockListView;

  @Mock
  RadioCellEditorView mockRadioView;

  EnumFormCellEditor editor;

  @Before
  public void before() {
    when(mockGinInjector.createListCellEditorView()).thenReturn(mockListView);
    when(mockGinInjector.createRadioCellEditorView()).thenReturn(mockRadioView);
    editor = new EnumFormCellEditor(mockDivView, mockGinInjector);
  }

  private List<String> getValuesForDropdownList() {
    List<String> values = new ArrayList<>();
    for (int i = 0; i <= EnumFormCellEditor.MAX_RADIO_BUTTONS; i++) {
      values.add(Integer.toString(i));
    }
    return values;
  }

  @Test
  public void testConfigureRadios() {
    List<String> values = new ArrayList<>();
    for (int i = 0; i < EnumFormCellEditor.MAX_RADIO_BUTTONS; i++) {
      values.add(Integer.toString(i));
    }
    editor.configure(values);
    verify(mockRadioView).configure(values);
  }

  @Test
  public void testConfigureDropdownList() {
    List<String> values = getValuesForDropdownList();
    editor.configure(values);
    verify(mockListView).configure(values);
  }

  @Test
  public void testSetNull() {
    List<String> values = Arrays.asList("one", "two");
    editor.configure(values);
    editor.setValue(null);
    verify(mockRadioView, never()).setValue(anyInt());
  }

  @Test
  public void testSetEmpty() {
    List<String> values = Arrays.asList("one", "two");
    editor.configure(values);
    editor.setValue("");
    verify(mockRadioView, never()).setValue(anyInt());
  }

  @Test
  public void testSetValueRadios() {
    List<String> values = Arrays.asList("one", "two");
    editor.configure(values);
    editor.setValue("one");
    verify(mockRadioView).setValue(0);
  }

  @Test
  public void testSetValueList() {
    List<String> values = getValuesForDropdownList();
    editor.configure(values);
    editor.setValue("0");
    verify(mockListView).setValue(0);
  }

  @Test
  public void testGetNull() {
    when(mockRadioView.getValue()).thenReturn(null);
    List<String> values = Arrays.asList("one", "two");
    editor.configure(values);
    assertEquals(null, editor.getValue());
  }

  @Test
  public void testGetValueRadio() {
    when(mockRadioView.getValue()).thenReturn(1);
    List<String> values = Arrays.asList("one", "two");
    editor.configure(values);
    assertEquals("two", editor.getValue());
  }

  @Test
  public void testGetValueList() {
    when(mockListView.getValue()).thenReturn(1);
    List<String> values = getValuesForDropdownList();
    editor.configure(values);
    assertEquals("1", editor.getValue());
  }
}
