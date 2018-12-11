package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Unit test for ColumnModelsViewWidget
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidgetTest {

	AdapterFactory adapterFactory;
	@Mock
	ColumnModelsViewBase mockBaseView;
	@Mock
	ColumnModelsView mockViewer;
	@Mock
	ColumnModelsEditorWidget mockEditor;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseClientAsync mockSynapseClient;
	ColumnModelsWidget widget;
	@Mock
	EntityBundle mockBundle;
	@Mock
	EntityView mockView;
	@Mock
	List<ColumnModel> mockDefaultColumnModels;
	@Mock
	TableUpdateTransactionRequest mockTableSchemaChangeRequest;
	@Mock
	JobTrackingWidget mockJobTrackingWidget;
	@Mock
	TableUpdateRequest mockTableUpdateRequest;
	@Mock
	ViewDefaultColumns mockFileViewDefaultColumns;
	@Captor
	ArgumentCaptor<ViewScope> viewScopeCaptor;
	@Mock
	List<String> mockViewScopeIds;
	@Mock
	ColumnModelPage mockColumnModelPage1;
	@Mock
	ColumnModelPage mockColumnModelPage2;
	@Mock
	List<ColumnModel> mockAnnotationColumnsPage1;
	@Mock
	List<ColumnModel> mockAnnotationColumnsPage2;
	@Mock
	EventBus mockEventBus;
	public static final String NEXT_PAGE_TOKEN = "nextPageToken";

	TableEntity table;
	TableBundle tableBundle;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		adapterFactory = new AdapterFactoryImpl();
		table = new TableEntity();
		table.setId("syn123");
		tableBundle = new TableBundle();
		when(mockBundle.getEntity()).thenReturn(table);
		when(mockBundle.getTableBundle()).thenReturn(tableBundle);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer);
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
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, mockEditor, mockJobTrackingWidget, mockFileViewDefaultColumns);
		when(mockEditor.validate()).thenReturn(true);
		when(mockTableSchemaChangeRequest.getChanges()).thenReturn(Collections.singletonList(mockTableUpdateRequest));
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		
		when(mockView.getScopeIds()).thenReturn(mockViewScopeIds);
		when(mockView.getType()).thenReturn(org.sagebionetworks.repo.model.table.ViewType.file);
		when(mockView.getViewTypeMask()).thenReturn(null);
		when(mockColumnModelPage1.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockColumnModelPage1.getResults()).thenReturn(mockAnnotationColumnsPage1);
		when(mockColumnModelPage2.getNextPageToken()).thenReturn(null);
		when(mockColumnModelPage2.getResults()).thenReturn(mockAnnotationColumnsPage2);
		
		AsyncMockStubber.callSuccessWith(mockColumnModelPage1, mockColumnModelPage2).when(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), anyString(), any(AsyncCallback.class));
		when(mockGinInjector.getEventBus()).thenReturn(mockEventBus);
	}
	
	@Test
	public void testConstruction() {
		verify(mockBaseView).setViewer(mockViewer);
		verify(mockBaseView).setEditor(mockEditor);
		verify(mockBaseView).setJobTrackingWidget(any(IsWidget.class));
		verify(mockBaseView).setJobTrackingWidgetVisible(false);
	}
	
	@Test
	public void testConfigure(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(false);
	}
	
	@Test
	public void testConfigureView(){
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(true);
	}
	
	@Test
	public void testGetDefaultColumnsForView() {
		boolean isEditable = true;
		
		when(mockFileViewDefaultColumns.getDefaultViewColumns(anyBoolean(), anyBoolean())).thenReturn(mockDefaultColumnModels);
		when(mockView.getType()).thenReturn(org.sagebionetworks.repo.model.table.ViewType.file);
		when(mockBundle.getEntity()).thenReturn(mockView);
		tableBundle.setColumnModels(TableModelTestUtils.createOneOfEachType(true));
		widget.configure(mockBundle, isEditable);
		widget.getDefaultColumnsForView();
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}
	
	@Test
	public void testGetDefaultColumnsForProjectView() {
		boolean isEditable = true;
		when(mockFileViewDefaultColumns.getDefaultViewColumns(anyBoolean(), anyBoolean())).thenReturn(mockDefaultColumnModels);
		when(mockView.getType()).thenReturn(org.sagebionetworks.repo.model.table.ViewType.project);
		when(mockBundle.getEntity()).thenReturn(mockView);
		tableBundle.setColumnModels(TableModelTestUtils.createOneOfEachType(true));
		widget.configure(mockBundle, isEditable);
		widget.getDefaultColumnsForView();
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}
	
	@Test
	public void testGetPossibleColumnModelsForViewScope() {
		// test is set up so that rpc successfully returns 2 pages, and then stops.
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		when(mockView.getType()).thenReturn(null);
		Long viewScopeMask = 1234L;
		when(mockView.getViewTypeMask()).thenReturn(viewScopeMask);

		widget.configure(mockBundle, isEditable);
		
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(viewScopeCaptor.capture(), eq(firstPageToken), any(AsyncCallback.class));
		//verify scope
		ViewScope viewScope = viewScopeCaptor.getValue();
		assertEquals(mockViewScopeIds, viewScope.getScope());
		assertEquals(viewScopeMask, viewScope.getViewTypeMask());
		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), eq(NEXT_PAGE_TOKEN), any(AsyncCallback.class));
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
	}
	
	@Test
	public void testGetPossibleColumnModelsForViewScopeWithViewType() {
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		org.sagebionetworks.repo.model.table.ViewType viewtype = org.sagebionetworks.repo.model.table.ViewType.file_and_table;
		when(mockView.getType()).thenReturn(viewtype);
		when(mockView.getViewTypeMask()).thenReturn(null);
		
		widget.configure(mockBundle, isEditable);
		
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(viewScopeCaptor.capture(), eq(firstPageToken), any(AsyncCallback.class));
		//verify scope
		ViewScope viewScope = viewScopeCaptor.getValue();
		assertEquals(mockViewScopeIds, viewScope.getScope());
		assertEquals(viewtype, viewScope.getViewType());
	}
	
	@Test
	public void testGetPossibleColumnModelsForViewScopeFailure() {
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		
		String error = "error message getting annotation column models";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), anyString(), any(AsyncCallback.class));
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), eq(firstPageToken), any(AsyncCallback.class));
		verify(mockBaseView).showError(error);
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor).configure(TableType.table, schema);
		verify(mockBaseView).showEditor();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEditable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// should fail
		widget.onEditColumns();
	}
	
	private AsynchronousProgressHandler onSave() {
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// Show the dialog
		widget.onEditColumns();
		when(mockEditor.getEditedColumnModels()).thenReturn(TableModelTestUtils.createOneOfEachType(false));
		// Now call save
		widget.onSave();
		boolean isDeterminate = false;
		ArgumentCaptor<AsynchronousProgressHandler> captor = ArgumentCaptor.forClass(AsynchronousProgressHandler.class);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(ColumnModelsWidget.UPDATING_SCHEMA), eq(isDeterminate), eq(AsynchType.TableTransaction), eq(mockTableSchemaChangeRequest), captor.capture());
		return captor.getValue();
	}
	
	@Test
	public void testOnSaveSuccess() throws JSONObjectAdapterException{
		onSave().onComplete(null);
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);
		
		verify(mockBaseView).setLoading();
		verify(mockBaseView).hideEditor();
		verify(mockBaseView).hideErrors();
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testOnPrimaryAsyncCancelled(){
		onSave().onCancel();
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);
		verify(mockBaseView, times(2)).showEditor();
	}
	
	@Test
	public void testOnPrimaryAsyncFailure(){
		String errorMessage = "error during schema update";
		Exception ex = new Exception(errorMessage);
		onSave().onFailure(ex);
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);
		verify(mockBaseView).showError(errorMessage);
	}
	
	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException{
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// Show the dialog
		widget.onEditColumns();
		when(mockEditor.validate()).thenReturn(false);
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
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// Show the dialog
		widget.onEditColumns();
		String errorMessage = "Something went wrong";
		AsyncMockStubber.callFailureWith(new RestServiceException(errorMessage)).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(),  anyList(), any(AsyncCallback.class));
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
	public void testGetTableType() {
		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.file));
		assertEquals(TableType.files, TableType.getTableType(mockView));
		
		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.file_and_table));
		assertEquals(TableType.files_tables, TableType.getTableType(mockView));
		
		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.project));
		assertEquals(TableType.projects, TableType.getTableType(mockView));
		
		assertEquals(TableType.table, TableType.getTableType(table));
	}
	
}
