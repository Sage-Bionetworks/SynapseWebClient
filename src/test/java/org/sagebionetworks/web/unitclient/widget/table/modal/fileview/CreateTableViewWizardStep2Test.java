package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2.DELETE_PLACEHOLDER_FAILURE_MESSAGE;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2.DELETE_PLACEHOLDER_SUCCESS_MESSAGE;
import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.FOLDER;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelRequest;
import org.sagebionetworks.repo.model.table.ViewColumnModelResponse;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;



public class CreateTableViewWizardStep2Test {

	@Mock
	ColumnModelsEditorWidget mockEditor;
	@Mock
	ModalWizardWidget mockWizardPresenter;
	String parentId;
	CreateTableViewWizardStep2 widget;
	@Mock
	EntityView mockEntityView;
	@Mock
	TableEntity mockTableEntity;
	@Mock
	SubmissionView mockSubmissionView;
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	List<ColumnModel> mockDefaultColumnModels;
	@Mock
	List<ColumnModel> mockDefaultProjectColumnModels;
	@Mock
	List<ColumnModel> mockDefaultSubmissionViewColumnModels;


	@Mock
	JobTrackingWidget mockJobTrackingWidget;
	@Mock
	TableUpdateTransactionRequest mockTableSchemaChangeRequest;
	@Mock
	CreateTableViewWizardStep2View mockView;
	@Mock
	TableUpdateRequest mockTableUpdateRequest;
	@Mock
	ViewDefaultColumns mockFileViewDefaultColumns;
	@Captor
	ArgumentCaptor<ViewScope> viewScopeCaptor;
	@Mock
	List<String> mockViewScopeIds;
	@Mock
	List<String> mockSubmissionViewScopeIds;
	@Mock
	List<ColumnModel> mockAnnotationColumnsPage1;
	@Mock
	List<ColumnModel> mockAnnotationColumnsPage2;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Captor
	ArgumentCaptor<ModalWizardWidget.WizardCallback> wizardCallbackCaptor;
	@Captor
	ArgumentCaptor<ViewColumnModelRequest> viewColumnModelRequestCaptor;
	@Mock
	ViewColumnModelResponse mockViewColumnModelResponsePage1;
	@Mock
	ViewColumnModelResponse mockViewColumnModelResponsePage2;

	public static final String NEXT_PAGE_TOKEN = "nextPageToken";
	public static final String ENTITY_ID = "syn109234";

	public static final TableType filesFoldersTablesView = new TableType(EntityView.class, FILE | FOLDER | TABLE);

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockTableEntity.getId()).thenReturn(ENTITY_ID);
		when(mockEntityView.getId()).thenReturn(ENTITY_ID);
		when(mockSubmissionView.getId()).thenReturn(ENTITY_ID);

		widget = new CreateTableViewWizardStep2(mockView, mockEditor, mockSynapseClient, mockJobTrackingWidget, mockFileViewDefaultColumns, mockJsClient, mockJsniUtils);

		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		when(mockEditor.validate()).thenReturn(true);
		when(mockTableSchemaChangeRequest.getChanges()).thenReturn(Collections.singletonList(mockTableUpdateRequest));
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		when(mockFileViewDefaultColumns.getDefaultViewColumns(TableType.file_view)).thenReturn(mockDefaultColumnModels);
		when(mockFileViewDefaultColumns.getDefaultViewColumns(filesFoldersTablesView)).thenReturn(mockDefaultColumnModels);
		when(mockFileViewDefaultColumns.getDefaultViewColumns(TableType.project_view)).thenReturn(mockDefaultProjectColumnModels);
		when(mockFileViewDefaultColumns.getDefaultViewColumns(TableType.submission_view)).thenReturn(mockDefaultSubmissionViewColumnModels);
		when(mockEntityView.getScopeIds()).thenReturn(mockViewScopeIds);
		when(mockSubmissionView.getScopeIds()).thenReturn(mockSubmissionViewScopeIds);
		when(mockViewColumnModelResponsePage1.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockViewColumnModelResponsePage1.getResults()).thenReturn(mockAnnotationColumnsPage1);
		when(mockViewColumnModelResponsePage2.getNextPageToken()).thenReturn(null);
		when(mockViewColumnModelResponsePage2.getResults()).thenReturn(mockAnnotationColumnsPage2);		

	}

	@Test
	public void testConfigureTable() {
		widget.configure(mockTableEntity, TableType.table);
		verify(mockEditor).setAddDefaultColumnsButtonVisible(false);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(false);
		verify(mockEditor).configure(TableType.table, new ArrayList<ColumnModel>());
	}

	@Test
	public void testConfigureView() {
		verify(mockView).setJobTracker(any(Widget.class));
		verify(mockView).setEditor(any(Widget.class));

		widget.configure(mockEntityView, TableType.file_view);
		verify(mockEditor).configure(TableType.file_view, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(true);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}
	
	@Test
	public void testConfigureSubmissionView() {
		widget.configure(mockEntityView, TableType.submission_view);
		
		verify(mockEditor).configure(TableType.submission_view, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(true);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultSubmissionViewColumnModels);
	}


	@Test
	public void testConfigureProjectView() {
		verify(mockView).setJobTracker(any(Widget.class));
		verify(mockView).setEditor(any(Widget.class));

		widget.configure(mockEntityView, TableType.project_view);
		verify(mockEditor).configure(TableType.project_view, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultColumnsButtonVisible(true);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultProjectColumnModels);
	}

	@Test
	public void testGetPossibleColumnModelsForViewScope() {
		// test is set up so that rpc successfully returns 2 pages, and then stops.
		widget.configure(mockEntityView, TableType.file_view);

		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		
		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockViewScopeIds, TableType.file_view.getViewTypeMask().longValue(), firstPageToken);
		handler.onComplete(mockViewColumnModelResponsePage1);

		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		handler = verifyViewColumnModelRequest(mockViewScopeIds, TableType.file_view.getViewTypeMask().longValue(), NEXT_PAGE_TOKEN);
		handler.onComplete(mockViewColumnModelResponsePage2);
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
	}

	@Test
	public void testGetPossibleColumnModelsForProjectViewScope() {
		// test is set up so that rpc successfully returns 2 pages, and then stops.
		widget.configure(mockEntityView, TableType.project_view);

		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockViewScopeIds, TableType.project_view.getViewTypeMask().longValue(), firstPageToken);
		handler.onComplete(mockViewColumnModelResponsePage1);

		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		handler = verifyViewColumnModelRequest(mockViewScopeIds, TableType.project_view.getViewTypeMask().longValue(), NEXT_PAGE_TOKEN);
		handler.onComplete(mockViewColumnModelResponsePage2);
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
	}

	@Test
	public void testGetPossibleColumnModelsForSubmissionViewScope() {
		widget.configure(mockSubmissionView, TableType.submission_view);

		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		
		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockSubmissionViewScopeIds, null, firstPageToken);
		handler.onComplete(mockViewColumnModelResponsePage1);

		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		handler = verifyViewColumnModelRequest(mockSubmissionViewScopeIds, null, NEXT_PAGE_TOKEN);
		handler.onComplete(mockViewColumnModelResponsePage2);
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
	}

	
	@Test
	public void testGetPossibleColumnModelsForViewScopeFailure() {
		widget.configure(mockEntityView, filesFoldersTablesView);
		String error = "error message getting annotation column models";
		Exception ex = new Exception(error);
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);

		AsynchronousProgressHandler handler = verifyViewColumnModelRequest(mockViewScopeIds, filesFoldersTablesView.getViewTypeMask().longValue(), firstPageToken);
		handler.onFailure(ex);

		verify(mockWizardPresenter).setError(ex);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockEditor).asWidget();
	}

	@Test
	public void testOnPrimaryInvalid() {
		when(mockEditor.validate()).thenReturn(false);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
	}

	@Test
	public void testOnCancelSuccessfulCleanup() {
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockWizardPresenter).addCallback(wizardCallbackCaptor.capture());
		ModalWizardWidget.WizardCallback wizardCallback = wizardCallbackCaptor.getValue();
		widget.configure(mockTableEntity, TableType.table);

		// simulate user cancels
		wizardCallback.onCanceled();

		// verify attempt to clean up
		verify(mockJsClient).deleteEntityById(eq(ENTITY_ID), eq(true), any(AsyncCallback.class));
		verify(mockJsniUtils).consoleLog(DELETE_PLACEHOLDER_SUCCESS_MESSAGE + ENTITY_ID);
	}

	@Test
	public void testOnCancelFailedToCleanup() {
		String errorMessage = "failed to clean up placeholder entity";

		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockJsClient).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
		verify(mockWizardPresenter).addCallback(wizardCallbackCaptor.capture());
		ModalWizardWidget.WizardCallback wizardCallback = wizardCallbackCaptor.getValue();
		widget.configure(mockTableEntity, TableType.table);

		// simulate user cancels
		wizardCallback.onCanceled();

		// verify attempt to clean up
		verify(mockJsClient).deleteEntityById(eq(ENTITY_ID), eq(true), any(AsyncCallback.class));
		verify(mockJsniUtils).consoleError(DELETE_PLACEHOLDER_FAILURE_MESSAGE + ENTITY_ID + ": " + errorMessage);
	}

	private AsynchronousProgressHandler onPrimary() {
		widget.configure(mockTableEntity, TableType.table);
		widget.onPrimary();
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
	public void testOnPrimary() {
		onPrimary().onComplete(null);
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setJobTrackerVisible(true);
		inOrder.verify(mockView).setJobTrackerVisible(false);

		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).onFinished();
		verify(mockJsClient, never()).deleteEntityById(anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testOnPrimaryAsyncCancelled() {
		onPrimary().onCancel();
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setJobTrackerVisible(true);
		inOrder.verify(mockView).setJobTrackerVisible(false);

		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setErrorMessage(CreateTableViewWizardStep2.SCHEMA_UPDATE_CANCELLED);
	}

	@Test
	public void testOnPrimaryAsyncFailure() {
		String errorMessage = "error during schema update";
		Exception ex = new Exception(errorMessage);
		onPrimary().onFailure(ex);
		InOrder inOrder = inOrder(mockView);
		inOrder.verify(mockView).setJobTrackerVisible(true);
		inOrder.verify(mockView).setJobTrackerVisible(false);

		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setError(ex);
	}

	@Test
	public void testOnPrimaryFailure() {
		widget.configure(mockTableEntity, TableType.table);
		String error = "error message";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setError(ex);
	}
}
