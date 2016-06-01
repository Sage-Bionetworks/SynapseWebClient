package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;
import org.sagebionetworks.web.unitclient.widget.table.v2.schema.ColumnModelTableRowEditorStub;
import org.sagebionetworks.web.unitclient.widget.table.v2.schema.ColumnModelTableRowViewerStub;

import com.google.gwt.user.client.rpc.AsyncCallback;



public class CreateTableViewWizardStep2Test {

	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	ModalPresenter mockWizardPresenter;
	@Mock
	ColumnModelsView mockColumnModelsView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	KeyboardNavigationHandler mockKeyboardNavigationHandler;
	String parentId;
	CreateTableViewWizardStep2 widget;
	@Mock
	FileView viewEntity;
	@Mock
	ColumnModelTableRowViewer mockColumnModelTableRowViewer;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockColumnModelsView);
		when(mockGinInjector.createKeyboardNavigationHandler()).thenReturn(mockKeyboardNavigationHandler);
		when(mockGinInjector.createNewColumnModelTableRowViewer()).thenAnswer(new Answer<ColumnModelTableRowViewer>() {
			@Override
			public ColumnModelTableRowViewer answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowViewerStub();
			}
		});
		when(mockGinInjector.createColumnModelEditorWidget()).thenAnswer(new Answer<ColumnModelTableRowEditorWidget >() {
			@Override
			public ColumnModelTableRowEditorWidget answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowEditorStub();
			}
		});

		widget = new CreateTableViewWizardStep2(mockGinInjector, mockSynapseClient);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
	}
	
	@Test
	public void testConstruction(){
		verify(mockColumnModelsView).setPresenter(widget);
		verify(mockColumnModelsView).setAddAllAnnotationsButtonVisible(true);
		verify(mockColumnModelsView).setAddDefaultFileColumnsButtonVisible(true);
	}
	
	@Test
	public void testConfigure(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		
		widget.configure(viewEntity, TableType.view, schema);
		verify(mockColumnModelsView).configure(ViewType.EDITOR, isEditable);
		verify(mockColumnModelsView, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	

	@Test
	public void testAddNewColumn(){
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(viewEntity, TableType.view, schema);
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
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(viewEntity, TableType.view, schema);
		// Show the dialog
		widget.onEditColumns();
		// Add a column
		ColumnModelTableRowEditorWidget editor = widget.addNewColumn();
		editor.setColumnName("a name");
		List<ColumnModel> expectedNewScheam = new LinkedList<ColumnModel>(schema);
		expectedNewScheam.add(ColumnModelUtils.extractColumnModel(editor));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setTableSchema(any(FileView.class), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		//TODO: determine if another page is necessary
		verify(mockWizardPresenter).onFinished();
	}
	
	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException{
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(viewEntity, TableType.view, schema);
		// Show the dialog
		widget.onEditColumns();
		
		// Add an invalid  column
		ColumnModelTableRowEditorStub editor = (ColumnModelTableRowEditorStub) widget.addNewColumn();
		editor.setValid(false);
		editor.setColumnName("a name");
		
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
		verify(mockWizardPresenter).setLoading(false);
	}
	
	@Test
	public void testOnSaveFailure() throws JSONObjectAdapterException{
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(viewEntity, TableType.view, schema);
		// Add a column
		ColumnModelTableRowEditorWidget editor = widget.addNewColumn();
		editor.setColumnName("a name");
		String errorMessage = "Something went wrong";
		AsyncMockStubber.callFailureWith(new RestServiceException(errorMessage)).when(mockSynapseClient).setTableSchema(any(TableEntity.class), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockWizardPresenter).setErrorMessage(errorMessage);
	}
	
	@Test
	public void testSelectAll(){
		widget.configure(viewEntity, TableType.view);
		verify(mockColumnModelsView).setCanDelete(false);
		verify(mockColumnModelsView).setCanMoveUp(false);
		verify(mockColumnModelsView).setCanMoveDown(false);
		
		// Add three columns
		reset(mockColumnModelsView);
		ColumnModelTableRowEditorWidget one = widget.addNewColumn();
		verify(mockColumnModelsView).setCanDelete(false);
		verify(mockColumnModelsView).setCanMoveUp(false);
		verify(mockColumnModelsView).setCanMoveDown(false);
		
		ColumnModelTableRowEditorWidget two = widget.addNewColumn();
		// Start with two selected
		reset(mockColumnModelsView);
		two.setSelected(true);
		verify(mockColumnModelsView).setCanDelete(true);
		verify(mockColumnModelsView).setCanMoveUp(true);
		verify(mockColumnModelsView).setCanMoveDown(false);
		
		reset(mockColumnModelsView);
		ColumnModelTableRowEditorWidget three = widget.addNewColumn();
		// With a new row the second row can move down.
		verify(mockColumnModelsView).setCanDelete(true);
		verify(mockColumnModelsView).setCanMoveUp(true);
		verify(mockColumnModelsView).setCanMoveDown(true);;
		
		// select all
		reset(mockColumnModelsView);
		widget.selectAll();
		assertTrue(one.isSelected());
		assertTrue(two.isSelected());
		assertTrue(three.isSelected());
		// The select all must not attempt to change the state
		// of the buttons for each selection and instead 
		// update the state at the end of the selection.
		verify(mockColumnModelsView).setCanDelete(true);
		verify(mockColumnModelsView).setCanMoveUp(false);
		verify(mockColumnModelsView).setCanMoveDown(false);
	}
	
	@Test
	public void testSelectNone(){
		widget.configure(viewEntity, TableType.view);
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
		widget.configure(viewEntity, TableType.view);
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
