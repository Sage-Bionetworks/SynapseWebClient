package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseClientAsync mockSynapseClient;
	ColumnModelsEditorWidget widget;
	List<ColumnModel> schema;
	
	@Mock
	ColumnModel nonEditableColumn;
	List<ColumnModel> nonEditableColumns;
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
		when(mockGinInjector.getCookieProvider()).thenReturn(mockCookies);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("true");
		nonEditableColumns = new ArrayList<ColumnModel>();
		nonEditableColumns.add(nonEditableColumn);
		AsyncMockStubber.callSuccessWith(nonEditableColumns).when(mockSynapseClient).getDefaultColumnsForView(any(org.sagebionetworks.repo.model.table.ViewType.class), any(AsyncCallback.class));
		widget = new ColumnModelsEditorWidget(mockGinInjector, mockSynapseClient, adapterFactory);
		schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(schema);
	}
	
	@Test
	public void testConfigure(){
		verify(mockEditor).configure(ViewType.EDITOR, true);
		// All rows should be added to the editor
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockGinInjector, times(schema.size())).createColumnModelEditorWidget();
		// are the rows registered?
		verify(mockKeyboardNavigationHandler).removeAllRows();
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
		
		//attempting to add all columns again is a no-op if they all exist in the editor already
		widget.addColumns(schema);
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		
		//select all, delete, and add schema again (verify all columns from the schema have been re-added).
		widget.selectAll();
		widget.deleteSelected();
		widget.addColumns(schema);
		verify(mockEditor, times(schema.size() * 2)).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testAddNewColumn(){
		// This should add a new string column
		widget.addNewColumn();
		// the new row should be added to the keyboard navigator
		verify(mockKeyboardNavigationHandler, times(schema.size() + 1)).bindRow(any(RowOfWidgets.class));
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
		// checks selection state each time a column editor is added, and once when columns are initialized.
		verify(mockEditor, times(schema.size() + 1)).setCanDelete(false);
		verify(mockEditor, times(schema.size() + 1)).setCanMoveUp(false);
		verify(mockEditor, times(schema.size() + 1)).setCanMoveDown(false);
		
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
