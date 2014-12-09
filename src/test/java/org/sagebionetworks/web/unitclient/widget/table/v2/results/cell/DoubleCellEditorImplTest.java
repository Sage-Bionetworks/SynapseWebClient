package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DoubleCellEditorImpl;

public class DoubleCellEditorImplTest {

	CellEditorView mockView;
	DoubleCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CellEditorView.class);
		editor = new DoubleCellEditorImpl(mockView);
	}
	
	@Test
	public void testValid(){
		String[] goodValues = new String[]{
			"12",
			"-12.1",
			"-.123e+32",
		};
		for(String good: goodValues){
			when(mockView.getValue()).thenReturn(good);
			assertTrue(editor.isValid());
		}
	}
	
	@Test
	public void testNotValid(){
		when(mockView.getValue()).thenReturn("some junk");
		assertFalse(editor.isValid());
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(DoubleCellEditorImpl.VALUE_MUST_BE_A_DOUBLE);
	}
	
	@Test
	public void testValidState(){
		when(mockView.getValue()).thenReturn("123.456");
		assertTrue(editor.isValid());
		verify(mockView).setValidationState(ValidationState.NONE);
		verify(mockView).setHelpText("");
	}
}
