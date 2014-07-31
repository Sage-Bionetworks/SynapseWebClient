package org.sagebionetworks.web.unitclient.widget.table.v2;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowEditorPresenter;
import org.sagebionetworks.web.client.widget.table.v2.ColumnTypeViewEnum;

public class ColumnModelTableRowEditorPresenterTest {
	
	ColumnModelTableRowEditor mockEditor;
	ColumnModelTableRowEditorPresenter presenter;
	
	@Before
	public void before(){
		mockEditor = Mockito.mock(ColumnModelTableRowEditor.class);
		when(mockEditor.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		when(mockEditor.getMaxSize()).thenReturn("15");
		presenter = new ColumnModelTableRowEditorPresenter(mockEditor);
	}
	
	@Test
	public void testTypeChange(){
		// Change from a string to a boolean
		when(mockEditor.getColumnType()).thenReturn(ColumnTypeViewEnum.Boolean);
		presenter.onTypeChanged();
		verify(mockEditor, times(1)).setSizeFieldVisible(false);
		verify(mockEditor, times(1)).setMaxSize(null);
		
		// Now toggle it back to a string
		when(mockEditor.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		presenter.onTypeChanged();
		verify(mockEditor, times(1)).setSizeFieldVisible(true);
		// It should keep the original value
		verify(mockEditor, times(1)).setMaxSize("15");
		

	}
	
	@Test
	public void testNoChange(){
		// The type starts as a string so toggle should do nothing
		when(mockEditor.getColumnType()).thenReturn(ColumnTypeViewEnum.String);
		presenter.onTypeChanged();
		verify(mockEditor, never()).setSizeFieldVisible(anyBoolean());
		// It should keep the original value
		verify(mockEditor, never()).setMaxSize(anyString());
	}

}
