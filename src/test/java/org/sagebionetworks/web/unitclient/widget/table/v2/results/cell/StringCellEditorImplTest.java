package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell.CHARACTERS_OR_LESS;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell.MUST_BE;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;

public class StringCellEditorImplTest {

	CellEditorView mockView;
	StringEditorCell editor;
	Long maxSize;

	@Before
	public void before() {
		mockView = Mockito.mock(CellEditorView.class);
		editor = new StringEditorCell(mockView);
		maxSize = 3L;
		editor.setMaxSize(maxSize);
	}

	@Test
	public void testNotValid() {
		when(mockView.getValue()).thenReturn("1234");
		assertFalse(editor.isValid());
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(MUST_BE + maxSize + CHARACTERS_OR_LESS);
	}

	@Test
	public void testValidState() {
		when(mockView.getValue()).thenReturn("123");
		assertTrue(editor.isValid());
		verify(mockView).setValidationState(ValidationState.NONE);
		verify(mockView).setHelpText("");
	}
}
