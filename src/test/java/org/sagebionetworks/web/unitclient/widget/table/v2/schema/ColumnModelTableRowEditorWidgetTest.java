package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

public class ColumnModelTableRowEditorWidgetTest {

	CellFactory mockFactory;
	ColumnModelTableRowEditorView mockView;
	ColumnModelTableRowEditorWidgetImpl editor;
	ColumnModel columnModel;
	CellEditor mockStringEditor;
	CellEditor mockLinkEditor;
	CellEditor mockBooleanEditor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(ColumnModelTableRowEditorView.class);
		mockFactory = Mockito.mock(CellFactory.class);
		mockStringEditor = Mockito.mock(CellEditor.class);
		mockLinkEditor = Mockito.mock(CellEditor.class);
		mockBooleanEditor = Mockito.mock(CellEditor.class);
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		when(mockFactory.createEditor(cm)).thenReturn(mockStringEditor);
		cm = new ColumnModel();
		cm.setColumnType(ColumnType.LINK);
		when(mockFactory.createEditor(cm)).thenReturn(mockLinkEditor);
		cm = new ColumnModel();
		cm.setColumnType(ColumnType.BOOLEAN);
		when(mockFactory.createEditor(cm)).thenReturn(mockBooleanEditor);
		when(mockView.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		when(mockView.getMaxSize()).thenReturn("15");
		editor = new ColumnModelTableRowEditorWidgetImpl(mockView, mockFactory);
		columnModel = new ColumnModel();
		columnModel.setColumnType(ColumnType.STRING);
		columnModel.setMaximumSize(15L);
	}
	
	@Test
	public void testConfigure(){
		editor.configure(columnModel, null);
		verify(mockView).setSizeFieldVisible(true);
		verify(mockView).setMaxSize(ColumnModelTableRowEditorWidgetImpl.DEFAULT_STRING_SIZE);
		verify(mockView).setDefaultEditor(mockStringEditor);
		verify(mockView).setColumnType(ColumnTypeViewEnum.String);
	}
	
	@Test
	public void testTypeChange(){
		editor.configure(columnModel, null);
		reset(mockView);
		// Change from a string to a boolean
		when(mockView.getColumnType()).thenReturn(ColumnTypeViewEnum.Boolean);
		editor.onTypeChanged();
		verify(mockView, times(1)).setSizeFieldVisible(false);
		verify(mockView, times(1)).setMaxSize(null);
		verify(mockView).setDefaultEditor(mockBooleanEditor);
		
		// Now toggle it back to a string
		when(mockView.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		editor.onTypeChanged();
		verify(mockView, times(1)).setSizeFieldVisible(true);
		// It should keep the original value
		verify(mockView, times(1)).setMaxSize(ColumnModelTableRowEditorWidgetImpl.DEFAULT_STRING_SIZE);
		verify(mockView).setDefaultEditor(mockStringEditor);
	}
	
	@Test
	public void testChangeToLink(){
		editor.configure(columnModel, null);
		reset(mockView);
		// Change from a string to a boolean
		when(mockView.getColumnType()).thenReturn(ColumnTypeViewEnum.Link);
		editor.onTypeChanged();
		verify(mockView, times(1)).setSizeFieldVisible(true);
		// It should keep the original value
		verify(mockView, times(1)).setMaxSize(ColumnModelTableRowEditorWidgetImpl.MAX_STRING_SIZE);
		verify(mockView).setDefaultEditor(mockLinkEditor);
	}
	
	@Test
	public void testNoChange(){
		editor.configure(columnModel, null);
		reset(mockView);
		// The type starts as a string so toggle should do nothing
		when(mockView.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		editor.onTypeChanged();
		verify(mockView, never()).setSizeFieldVisible(anyBoolean());
		// It should keep the original value
		verify(mockView, never()).setMaxSize(anyString());
	}

}
