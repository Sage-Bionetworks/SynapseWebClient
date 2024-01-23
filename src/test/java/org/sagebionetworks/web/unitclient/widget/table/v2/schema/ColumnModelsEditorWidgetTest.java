package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidgetView;

@RunWith(MockitoJUnitRunner.class)
public class ColumnModelsEditorWidgetTest {

  ColumnModelsEditorWidget widget;

  private final String entityId = "syn1234";

  @Mock
  ColumnModelsEditorWidgetView mockView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  TableColumnSchemaEditorProps.OnColumnsUpdated mockOnColumnsUpdated;

  @Mock
  TableColumnSchemaEditorProps.OnCancel mockOnCancel;

  @Captor
  ArgumentCaptor<TableColumnSchemaEditorProps> propsCaptor;

  @Before
  public void before() {
    widget = new ColumnModelsEditorWidget(mockView, mockGlobalAppState);
  }

  @Test
  public void testConfigure() {
    widget.configure(entityId, mockOnColumnsUpdated, mockOnCancel);

    verify(mockView).renderComponent(propsCaptor.capture());

    TableColumnSchemaEditorProps props = propsCaptor.getValue();
    assertEquals(props.entityId, entityId);
    assertEquals(props.onColumnsUpdated, mockOnColumnsUpdated);
    assertEquals(props.onCancel, mockOnCancel);
    assertFalse(props.open);

    verifyZeroInteractions(mockGlobalAppState);
  }

  @Test
  public void testSetOpen() {
    widget.configure(entityId, mockOnColumnsUpdated, mockOnCancel);

    widget.setOpen(true);

    verify(mockView, times(2)).renderComponent(propsCaptor.capture());
    TableColumnSchemaEditorProps props = propsCaptor.getValue();
    assertEquals(props.entityId, entityId);
    assertEquals(props.onColumnsUpdated, mockOnColumnsUpdated);
    assertEquals(props.onCancel, mockOnCancel);
    assertTrue(props.open);
    verify(mockGlobalAppState).setIsEditing(true);

    widget.setOpen(false);

    verify(mockView, times(3)).renderComponent(propsCaptor.capture());
    props = propsCaptor.getValue();
    assertEquals(props.entityId, entityId);
    assertEquals(props.onColumnsUpdated, mockOnColumnsUpdated);
    assertEquals(props.onCancel, mockOnCancel);
    assertFalse(props.open);
    verify(mockGlobalAppState).setIsEditing(false);
  }
}
