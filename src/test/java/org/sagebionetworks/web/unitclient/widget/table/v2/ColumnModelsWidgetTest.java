package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableModelUtils;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

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
	PortalGinInjector mockGinInjector;
	SynapseClientAsync mockSynapseClient;
	TableModelUtils tableModelUtils;
	ColumnModelsWidget widget;
	
	@Before
	public void before(){
		mockBaseView = Mockito.mock(ColumnModelsViewBase.class);
		mockViewer = Mockito.mock(ColumnModelsView.class);
		mockEditor = Mockito.mock(ColumnModelsView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		adapterFactory = new AdapterFactoryImpl();
		tableModelUtils = new TableModelUtils(adapterFactory);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer, mockEditor);
		when(mockGinInjector.createNewColumnModelTableRowEditor()).thenAnswer(new Answer<ColumnModelTableRowEditor>() {
			@Override
			public ColumnModelTableRowEditor answer(InvocationOnMock invocation)
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
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, tableModelUtils);
		verify(mockBaseView, times(1)).setViewer(mockViewer);
		verify(mockBaseView, times(1)).setEditor(mockEditor);
	}
	
	@Test
	public void testConfigure(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		verify(mockViewer, times(1)).configure(ViewType.VIEWER, isEdtiable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor, times(1)).configure(ViewType.EDITOR, isEdtiable);
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockBaseView, times(1)).showEditor();
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEdtiable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// should fail
		widget.onEditColumns();
	}
	
	@Test
	public void testAddNewColumn(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// show the editor
		widget.onEditColumns();
		// This should add a new string column
		widget.addNewColumn();
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
		String tableId = "syn123";
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(tableId, schema, isEdtiable);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditor editor = widget.addNewColumn();
		editor.setColumnName("a name");
		List<ColumnModel> expectedNewScheam = new LinkedList<ColumnModel>(schema);
		expectedNewScheam.add(ColumnModelUtils.extractColumnModel(editor));
		List<String> results = tableModelUtils.toJSONList(expectedNewScheam);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).setTableSchema(anyString(), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		verify(mockBaseView).hideEditor();
		// the view should be configured with original columns, then again 
		// with the original columns after the save plus one new column
		verify(mockViewer, times(schema.size()*2+1)).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testOnSaveFailure() throws JSONObjectAdapterException{
		boolean isEdtiable = true;
		String tableId = "syn123";
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(tableId, schema, isEdtiable);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditor editor = widget.addNewColumn();
		editor.setColumnName("a name");
		String errorMessage = "Something went wrong";
		AsyncMockStubber.callFailureWith(new RestServiceException(errorMessage)).when(mockSynapseClient).setTableSchema(anyString(), any(List.class), any(AsyncCallback.class));
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
		String tableId = "syn123";
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		widget.configure(tableId, schema, isEdtiable);
		// Show the dialog
		widget.onEditColumns();
		verify(mockEditor).setCanDelete(false);
		verify(mockEditor).setCanMoveUp(false);
		verify(mockEditor).setCanMoveDown(false);
		
		// Add three columns
		reset(mockEditor);
		ColumnModelTableRowEditor one = widget.addNewColumn();
		verify(mockEditor).setCanDelete(false);
		verify(mockEditor).setCanMoveUp(false);
		verify(mockEditor).setCanMoveDown(false);
		
		ColumnModelTableRowEditor two = widget.addNewColumn();
		// Start with two selected
		reset(mockEditor);
		two.setSelected(true);
		verify(mockEditor).setCanDelete(true);
		verify(mockEditor).setCanMoveUp(true);
		verify(mockEditor).setCanMoveDown(false);
		
		reset(mockEditor);
		ColumnModelTableRowEditor three = widget.addNewColumn();
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
		String tableId = "syn123";
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		widget.configure(tableId, schema, isEdtiable);
		// Show the dialog
		widget.onEditColumns();
		// Add three columns
		ColumnModelTableRowEditor one = widget.addNewColumn();
		ColumnModelTableRowEditor two = widget.addNewColumn();
		// Start with two selected
		two.setSelected(true);
		ColumnModelTableRowEditor three = widget.addNewColumn();
		// select all
		widget.selectNone();
		assertFalse(one.isSelected());
		assertFalse(two.isSelected());
		assertFalse(three.isSelected());
	}
	
	@Test
	public void testToggleSelect(){
		boolean isEdtiable = true;
		String tableId = "syn123";
		List<ColumnModel> schema = new LinkedList<ColumnModel>();
		widget.configure(tableId, schema, isEdtiable);
		// Show the dialog
		widget.onEditColumns();
		// Add three columns
		ColumnModelTableRowEditor one = widget.addNewColumn();
		ColumnModelTableRowEditor two = widget.addNewColumn();
		// Start with two selected
		two.setSelected(true);
		ColumnModelTableRowEditor three = widget.addNewColumn();
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
