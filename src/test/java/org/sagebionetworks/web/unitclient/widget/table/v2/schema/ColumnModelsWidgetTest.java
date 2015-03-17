package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for ColumnModelsViewWidget
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidgetTest {

	AdapterFactory adapterFactory;
	ColumnModelsViewBase mockBaseView;
	ColumnModelsView mockViewer;
	ColumnModelsView mockEditor;
	EntityUpdatedHandler mockUpdateHandler;
	PortalGinInjector mockGinInjector;
	SynapseClientAsync mockSynapseClient;
	KeyboardNavigationHandler mockKeyboardNavigationHandler;
	ColumnModelsWidget widget;
	EntityBundle mockBundle;
	TableEntity table;
	TableBundle tableBundle;
	
	@Before
	public void before(){
		mockBaseView = Mockito.mock(ColumnModelsViewBase.class);
		mockViewer = Mockito.mock(ColumnModelsView.class);
		mockEditor = Mockito.mock(ColumnModelsView.class);
		mockBundle = Mockito.mock(EntityBundle.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockUpdateHandler = Mockito.mock(EntityUpdatedHandler.class);
		mockKeyboardNavigationHandler = Mockito.mock(KeyboardNavigationHandler.class);
		adapterFactory = new AdapterFactoryImpl();
		table = new TableEntity();
		table.setId("syn123");
		tableBundle = new TableBundle();
		when(mockBundle.getEntity()).thenReturn(table);
		when(mockBundle.getTableBundle()).thenReturn(tableBundle);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer, mockEditor);
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
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient);
		verify(mockBaseView, times(1)).setViewer(mockViewer);
		verify(mockBaseView, times(1)).setEditor(mockEditor);
	}
	
	@Test
	public void testConfigure(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		verify(mockViewer, times(1)).configure(ViewType.VIEWER, isEdtiable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor, times(1)).configure(ViewType.EDITOR, isEdtiable);
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockBaseView, times(1)).showEditor();
		// are the rows registered?
		verify(mockKeyboardNavigationHandler, times(1)).removeAllRows();
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEdtiable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// should fail
		widget.onEditColumns();
	}
	
	@Test
	public void testAddNewColumn(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// show the editor
		widget.onEditColumns();
		// This should add a new string column
		widget.addNewColumn();
		// the new row should be added to the keyboard navigator
		verify(mockKeyboardNavigationHandler).bindRow(any(RowOfWidgets.class));
		// A string should be added...
		ColumnModel newModel = new ColumnModel();
		newModel.setColumnType(ColumnModelsWidget.DEFAULT_NEW_COLUMN_TYPE);
		newModel.setMaximumSize(ColumnModelsWidget.DEFAULT_STRING_MAX_SIZE);
		schema.add(newModel);
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test
	public void testOnSaveSuccess() throws JSONObjectAdapterException{
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditorWidget editor = widget.addNewColumn();
		editor.setColumnName("a name");
		List<ColumnModel> expectedNewScheam = new LinkedList<ColumnModel>(schema);
		expectedNewScheam.add(ColumnModelUtils.extractColumnModel(editor));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setTableSchema(any(TableEntity.class), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		verify(mockBaseView).hideEditor();
		verify(mockBaseView).hideErrors();
		// Save success should be called.
		verify(mockUpdateHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException{
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditorStub editor = (ColumnModelTableRowEditorStub) widget.addNewColumn();
		editor.setValid(false);
		editor.setColumnName("a name");
		// Now call save
		widget.onSave();
		verify(mockBaseView, never()).setLoading();
		verify(mockBaseView, never()).hideEditor();
		verify(mockBaseView, never()).hideErrors();
		// Save success should be called.
		verify(mockBaseView).showError(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
	}
	
	@Test
	public void testOnSaveFailure() throws JSONObjectAdapterException{
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditorWidget editor = widget.addNewColumn();
		editor.setColumnName("a name");
		String errorMessage = "Something went wrong";
		AsyncMockStubber.callFailureWith(new RestServiceException(errorMessage)).when(mockSynapseClient).setTableSchema(any(TableEntity.class), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		// The editor must not be hidden on an error.
		verify(mockBaseView, never()).hideEditor();
		verify(mockBaseView).showError(errorMessage);
		// only the original columns should be applied to the view.
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testSelectAll(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
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
		boolean isEdtiable = true;
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
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
		boolean isEdtiable = true;
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEdtiable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
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
