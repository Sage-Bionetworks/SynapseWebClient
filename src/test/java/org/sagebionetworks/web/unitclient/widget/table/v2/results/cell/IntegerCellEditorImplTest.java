package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.IntegerCellEditorImpl;

public class IntegerCellEditorImplTest {

	CellEditorView mockView;
	IntegerCellEditorImpl editor;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CellEditorView.class);
		editor = new IntegerCellEditorImpl(mockView);
	}
	
	@Test
	public void testValid(){
		String[] goodValues = new String[]{
			"12",
			"-12",
			"+456456456457456",
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
		verify(mockView).setHelpText(IntegerCellEditorImpl.VALUE_MUST_BE_AN_INTEGER);
	}
	
	@Test
	public void testValidState(){
		when(mockView.getValue()).thenReturn("123");
		assertTrue(editor.isValid());
		verify(mockView).setValidationState(ValidationState.NONE);
		verify(mockView).setHelpText("");
	}
}
