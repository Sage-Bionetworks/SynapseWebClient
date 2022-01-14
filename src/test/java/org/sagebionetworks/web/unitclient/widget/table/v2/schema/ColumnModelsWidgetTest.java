package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;

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
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelResponse;
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
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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
	SubmissionView mockSubmissionView;
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
	List<ColumnModel> mockAnnotationColumnsPage1;
	@Mock
	List<ColumnModel> mockAnnotationColumnsPage2;
	@Mock
	EventBus mockEventBus;
	@Mock
	SynapseAlert mockSynAlert;
	@Captor
	ArgumentCaptor<ViewColumnModelRequest> viewColumnModelRequestCaptor;
	@Captor
	ArgumentCaptor<List<ColumnModelTableRow>> columnModelTableRowsCaptor;
	@Mock
	ViewColumnModelResponse mockViewColumnModelResponsePage1;
	@Mock
	ViewColumnModelResponse mockViewColumnModelResponsePage2;
	
	public static final String NEXT_PAGE_TOKEN = "nextPageToken";

	TableEntity table;
	TableBundle tableBundle;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		adapterFactory = new AdapterFactoryImpl();
		table = new TableEntity();
		table.setId("syn123");
		tableBundle = new TableBundle();
		when(mockBundle.getEntity()).thenReturn(table);
		when(mockBundle.getTableBundle()).thenReturn(tableBundle);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer);
		when(mockGinInjector.createColumnModelEditorWidget()).thenAnswer(new Answer<ColumnModelTableRowEditorWidget>() {
			@Override
			public ColumnModelTableRowEditorWidget answer(InvocationOnMock invocation) throws Throwable {
				return new ColumnModelTableRowEditorStub();
			}
		});
		when(mockGinInjector.createNewColumnModelTableRowViewer()).thenAnswer(new Answer<ColumnModelTableRowViewer>() {
			@Override
			public ColumnModelTableRowViewer answer(InvocationOnMock invocation) throws Throwable {
				return new ColumnModelTableRowViewerStub();
			}
		});
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, mockEditor, mockJobTrackingWidget, mockFileViewDefaultColumns, mockSynAlert);
		when(mockEditor.validate()).thenReturn(true);
		when(mockTableSchemaChangeRequest.getChanges()).thenReturn(Collections.singletonList(mockTableUpdateRequest));
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));

		when(mockView.getScopeIds()).thenReturn(mockViewScopeIds);
		when(mockView.getType()).thenReturn(org.sagebionetworks.repo.model.table.ViewType.file);
		when(mockView.getViewTypeMask()).thenReturn(null);
		when(mockViewColumnModelResponsePage1.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockViewColumnModelResponsePage1.getResults()).thenReturn(mockAnnotationColumnsPage1);
		when(mockViewColumnModelResponsePage2.getNextPageToken()).thenReturn(null);
		when(mockViewColumnModelResponsePage2.getResults()).thenReturn(mockAnnotationColumnsPage2);		
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
	public void testConfigure() {
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer).addColumns(columnModelTableRowsCaptor.capture());
		assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(false);
	}

	@Test
	public void testConfigureView() {
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer).addColumns(columnModelTableRowsCaptor.capture());
		assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(true);
	}
	
	@Test
	public void testConfigureSubmissionView() {
		boolean isEditable = true;
		when(mockBundle.getEntity()).thenReturn(mockSubmissionView);
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer).addColumns(columnModelTableRowsCaptor.capture());
		assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(true);
	}

	@Test
	public void testGetDefaultColumnsForView() {
		boolean isEditable = true;

		when(mockFileViewDefaultColumns.getDefaultViewColumns(any(TableType.class))).thenReturn(mockDefaultColumnModels);
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
		when(mockFileViewDefaultColumns.getDefaultViewColumns(any(TableType.class))).thenReturn(mockDefaultColumnModels);
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

		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockViewScopeIds, viewScopeMask, firstPageToken);
		handler.onComplete(mockViewColumnModelResponsePage1);
		
		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		handler = verifyViewColumnModelRequest(mockViewScopeIds, viewScopeMask, NEXT_PAGE_TOKEN);
		handler.onComplete(mockViewColumnModelResponsePage2);
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
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
		
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		
		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockViewScopeIds, null, firstPageToken);
		handler.onFailure(ex);
		
		verify(mockSynAlert).handleException(ex);
		verify(mockBaseView).resetSaveButton();
	}

	@Test
	public void testOnEditColumns() {
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor).configure(TableType.table, schema);
		verify(mockBaseView).showEditor();
	}

	@Test(expected = IllegalStateException.class)
	public void testOnEditNonEditable() {
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

	private AsynchronousProgressHandler verifyViewColumnModelRequest(List<String> scopeIds, Long viewMask, String nextPageToken) {
		ArgumentCaptor<AsynchronousProgressHandler> captor = ArgumentCaptor.forClass(AsynchronousProgressHandler.class);
		boolean isDeterminate = false;
		verify(mockJobTrackingWidget).startAndTrackJob(eq(ColumnModelsWidget.RETRIEVING_DATA), eq(isDeterminate), eq(AsynchType.ViewColumnModelRequest), viewColumnModelRequestCaptor.capture(), captor.capture());
		ViewColumnModelRequest capturedRequest = viewColumnModelRequestCaptor.getValue();
		assertEquals(scopeIds, capturedRequest.getViewScope().getScope());
		assertEquals(viewMask, capturedRequest.getViewScope().getViewTypeMask());
		assertEquals(nextPageToken, capturedRequest.getNextPageToken());
		reset(mockJobTrackingWidget);
		return captor.getValue();
	}
	
	@Test
	public void testOnSaveSuccess() throws JSONObjectAdapterException {
		onSave().onComplete(null);
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);

		verify(mockBaseView).setLoading();
		verify(mockBaseView).hideEditor();
		verify(mockSynAlert).clear();
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testOnPrimaryAsyncCancelled() {
		onSave().onCancel();
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);
		verify(mockBaseView, times(2)).showEditor();
	}

	@Test
	public void testOnPrimaryAsyncFailure() {
		String errorMessage = "error during schema update";
		Exception ex = new Exception(errorMessage);
		onSave().onFailure(ex);
		InOrder inOrder = inOrder(mockBaseView);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(true);
		inOrder.verify(mockBaseView).setJobTrackingWidgetVisible(false);
		verify(mockSynAlert).handleException(ex);
		verify(mockBaseView).resetSaveButton();
	}

	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException {
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
		verify(mockSynAlert, never()).clear();
		// Save success should be called.
		verify(mockSynAlert).showError(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
	}

	@Test
	public void testOnSaveFailure() throws JSONObjectAdapterException {
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable);
		// Show the dialog
		widget.onEditColumns();
		String errorMessage = "Something went wrong";
		RestServiceException ex = new RestServiceException(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		// The editor must not be hidden on an error.
		verify(mockBaseView, never()).hideEditor();
		verify(mockSynAlert).handleException(ex);
		verify(mockBaseView).resetSaveButton();
		// only the original columns should be applied to the view.
		verify(mockViewer).addColumns(columnModelTableRowsCaptor.capture());
		assertEquals(schema.size(), columnModelTableRowsCaptor.getValue().size());
	}

	@Test
	public void testGetTableType() {
		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.file));
		assertEquals(TableType.file_view, TableType.getTableType(mockView));

		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.file_and_table));
		assertEquals(new TableType(EntityView.class, FILE | TABLE), TableType.getTableType(mockView));

		when(mockView.getViewTypeMask()).thenReturn(ViewTypeMask.getMaskForDepricatedType(org.sagebionetworks.repo.model.table.ViewType.project));
		assertEquals(TableType.project_view, TableType.getTableType(mockView));

		assertEquals(TableType.table, TableType.getTableType(table));
	}

}
