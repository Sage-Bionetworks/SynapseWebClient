package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit test for ColumnModelsViewWidget
 * 
 * @author jmhill
 *
 */
public class ColumnModelsEditorWidgetTest {

	AdapterFactory adapterFactory;
	@Mock
	ColumnModelsView mockEditor;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	KeyboardNavigationHandler mockKeyboardNavigationHandler;
	ColumnModelsEditorWidget widget;
	List<ColumnModel> schema;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		adapterFactory = new AdapterFactoryImpl();
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockEditor);
		when(mockGinInjector.createColumnModelEditorWidget()).thenAnswer(new Answer<ColumnModelTableRowEditorWidget >() {
			@Override
			public ColumnModelTableRowEditorWidget answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowEditorStub();
			}
		});
		when(mockGinInjector.createNewColumnModelTableRowViewer()).thenAnswer(new Answer<ColumnModelTableRowViewer>() {
			@Override
			public ColumnModelTableRowViewer answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowViewerStub();
			}
		});
		when(mockGinInjector.createKeyboardNavigationHandler()).thenReturn(mockKeyboardNavigationHandler);
		widget = new ColumnModelsEditorWidget(mockGinInjector);
		schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(schema);
	}
	
	@Test
	public void testConfigure(){
		verify(mockEditor).configure(ViewType.EDITOR, true);
		// All rows should be added to the editor
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		// are the rows registered?
		verify(mockKeyboardNavigationHandler).removeAllRows();
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test
	public void testAddNewColumn(){
		// This should add a new string column
		widget.addNewColumn();
		// the new row should be added to the keyboard navigator
		verify(mockKeyboardNavigationHandler).bindRow(any(RowOfWidgets.class));
		// A string should be added...
		ColumnModel newModel = new ColumnModel();
		newModel.setColumnType(ColumnModelsEditorWidget.DEFAULT_NEW_COLUMN_TYPE);
		newModel.setMaximumSize(ColumnModelsEditorWidget.DEFAULT_STRING_MAX_SIZE);
		schema.add(newModel);
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	
	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException{
		// Add a column
		ColumnModelTableRowEditorStub editor = (ColumnModelTableRowEditorStub) widget.addNewColumn();
		editor.setValid(false);
		editor.setColumnName("a name");
		// Now call validate
		assertFalse(widget.validate());
	}
	
	
	@Test
	public void testSelectAll(){
		verify(mockEditor).setCanDelete(false);
		verify(mockEditor).setCanMoveUp(false);
		verify(mockEditor).setCanMoveDown(false);
		
		// Add three columns
		reset(mockEditor);
		ColumnModelTableRowEditorWidget one = widget.addNewColumn();
		verify(mockEditor).setCanDelete(false);
		verify(mockEditor).setCanMoveUp(false);
		verify(mockEditor).setCanMoveDown(false);
		
		ColumnModelTableRowEditorWidget two = widget.addNewColumn();
		// Start with two selected
		reset(mockEditor);
		two.setSelected(true);
		verify(mockEditor).setCanDelete(true);
		verify(mockEditor).setCanMoveUp(true);
		verify(mockEditor).setCanMoveDown(false);
		
		reset(mockEditor);
		ColumnModelTableRowEditorWidget three = widget.addNewColumn();
		// With a new row the second row can move down.
		verify(mockEditor).setCanDelete(true);
		verify(mockEditor).setCanMoveUp(true);
		verify(mockEditor).setCanMoveDown(true);;
		
		// select all
		reset(mockEditor);
		widget.selectAll();
		assertTrue(one.isSelected());
		assertTrue(two.isSelected());
		assertTrue(three.isSelected());
		// The select all must not attempt to change the state
		// of the buttons for each selection and instead 
		// update the state at the end of the selection.
		verify(mockEditor).setCanDelete(true);
		verify(mockEditor).setCanMoveUp(false);
		verify(mockEditor).setCanMoveDown(false);
	}
	
	@Test
	public void testSelectNone(){
		// Add three columns
		ColumnModelTableRowEditorWidget one = widget.addNewColumn();
		ColumnModelTableRowEditorWidget two = widget.addNewColumn();
		// Start with two selected
		two.setSelected(true);
		ColumnModelTableRowEditorWidget three = widget.addNewColumn();
		// select all
		widget.selectNone();
		assertFalse(one.isSelected());
		assertFalse(two.isSelected());
		assertFalse(three.isSelected());
	}
	
	@Test
	public void testToggleSelect(){
		// Add three columns
		ColumnModelTableRowEditorWidget one = widget.addNewColumn();
		ColumnModelTableRowEditorWidget two = widget.addNewColumn();
		// Start with two selected
		two.setSelected(true);
		ColumnModelTableRowEditorWidget three = widget.addNewColumn();
		// select all
		widget.toggleSelect();
		assertFalse(one.isSelected());
		assertFalse(two.isSelected());
		assertFalse(three.isSelected());
		// do it again
		widget.toggleSelect();
		assertTrue(one.isSelected());
		assertTrue(two.isSelected());
		assertTrue(three.isSelected());
	}
	
}
