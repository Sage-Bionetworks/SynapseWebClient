package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor.CHARACTERS_OR_LESS;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor.ITEMS_OR_LESS;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor.MUST_BE;
import static org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor.VALID_JSON_ARRAY;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.JSONListCellEditorView;


@RunWith(MockitoJUnitRunner.class)
public class JSONListCellEditorImplTest {

	@Mock
	JSONListCellEditorView mockView;
	JSONArrayAdapter jsonArrayAdapter = new JSONArrayAdapterImpl();
	JSONListCellEditor editor;
	@Mock
	PortalGinInjector mockGinInjector;

	ColumnModel columnModel;
	
	@Before
	public void before() {
		editor = new JSONListCellEditor(mockView, jsonArrayAdapter, mockGinInjector);
		columnModel = new ColumnModel();
		columnModel.setColumnType(ColumnType.STRING_LIST);
		editor.setColumnModel(columnModel);
	}

	@Test
	public void testNotValidJsonArray() {
		when(mockView.getValue()).thenReturn("1923");
		
		assertFalse(editor.isValid());
		
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(MUST_BE + VALID_JSON_ARRAY);
	}
	
	@Test
	public void testInvalidSize() {
		Long maxSize = 3L;
		columnModel.setMaximumSize(maxSize);
		when(mockView.getValue()).thenReturn("[\"1234\"]");
		
		assertFalse(editor.isValid());
		
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(MUST_BE + maxSize + CHARACTERS_OR_LESS);
	}
	

	@Test
	public void testInvalidLength() {
		Long maxListLength = 2L;
		columnModel.setMaximumListLength(maxListLength);
		when(mockView.getValue()).thenReturn("[\"a\", \"b\", \"c\"]");
		
		assertFalse(editor.isValid());
		
		verify(mockView).setValidationState(ValidationState.ERROR);
		verify(mockView).setHelpText(MUST_BE + maxListLength + ITEMS_OR_LESS);
	}


	@Test
	public void testValidState() {
		Long maxSize = 3L;
		columnModel.setMaximumSize(maxSize);
		Long maxListLength = 3L;
		columnModel.setMaximumListLength(maxListLength);
		when(mockView.getValue()).thenReturn("[\"a\", \"b\", \"c\"]");
		
		assertTrue(editor.isValid());
		
		verify(mockView).setValidationState(ValidationState.NONE);
		verify(mockView).setHelpText("");
	}
}
