package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellEditorImpl;

public class EntityIdCellEditorImplTest {

	CellEditorView mockView;
	EntityIdCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CellEditorView.class);
		editor = new EntityIdCellEditorImpl(mockView);
	}
	
	@Test
	public void testPlaceHolder(){
		verify(mockView).setPlaceholder(EntityIdCellEditorImpl.PLACE_HOLDER);
	}
	
	@Test
	public void testNotValid(){
		when(mockView.getValue()).thenReturn("some junk");
		assertFalse(editor.isValid());
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(EntityIdCellEditorImpl.MUST_BE_OF_THE_FORM_SYN123);
	}
	
	@Test
	public void testValid(){
		when(mockView.getValue()).thenReturn(" syn123 ");
		assertTrue(editor.isValid());
		verify(mockView).setValidationState(ValidationState.NONE);
		verify(mockView).setHelpText("");
	}
	
	@Test
	public void testValidCaps(){
		when(mockView.getValue()).thenReturn("SYN9999");
		assertTrue(editor.isValid());
	}
	
	
	@Test
	public void testValidEmpty(){
		// empty should be valid
		when(mockView.getValue()).thenReturn("");
		assertTrue(editor.isValid());
	}
	
	@Test
	public void testValidNull(){
		// null should be valid
		when(mockView.getValue()).thenReturn(null);
		assertTrue(editor.isValid());
	}
	
	@Test
	public void testValidVersion(){
		// null should be valid
		when(mockView.getValue()).thenReturn("syn123.34");
		assertTrue(editor.isValid());
	}
	
	@Test
	public void testValidBad(){
		String[] badValues = new String[]{
			"ssyn123.34",
			"syn123.34.1",
			"syn123.34e",
			"syn123a.b",
		};
		for(String bad: badValues){
			when(mockView.getValue()).thenReturn(bad);
			assertFalse(editor.isValid());
		}

	}
}
