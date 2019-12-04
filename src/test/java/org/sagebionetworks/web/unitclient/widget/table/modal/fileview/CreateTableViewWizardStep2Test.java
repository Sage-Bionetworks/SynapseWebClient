package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2.DELETE_PLACEHOLDER_FAILURE_MESSAGE;
import static org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2.DELETE_PLACEHOLDER_SUCCESS_MESSAGE;
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
import org.sagebionetworks.repo.model.table.ColumnModelPage;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewScope;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
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
	EntityView viewEntity;
	@Mock
	TableEntity tableEntity;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	List<ColumnModel> mockDefaultColumnModels;
	@Mock
	List<ColumnModel> mockDefaultProjectColumnModels;

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
	ColumnModelPage mockColumnModelPage1;
	@Mock
	ColumnModelPage mockColumnModelPage2;
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

	public static final String NEXT_PAGE_TOKEN = "nextPageToken";
	public static final String ENTITY_ID = "syn109234";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(tableEntity.getId()).thenReturn(ENTITY_ID);
		when(viewEntity.getId()).thenReturn(ENTITY_ID);

		widget = new CreateTableViewWizardStep2(mockView, mockEditor, mockSynapseClient, mockJobTrackingWidget, mockFileViewDefaultColumns, mockJsClient, mockJsniUtils);

		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		when(mockEditor.validate()).thenReturn(true);
		when(mockTableSchemaChangeRequest.getChanges()).thenReturn(Collections.singletonList(mockTableUpdateRequest));
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		when(mockFileViewDefaultColumns.getDefaultViewColumns(eq(true), anyBoolean())).thenReturn(mockDefaultColumnModels);
		when(mockFileViewDefaultColumns.getDefaultViewColumns(eq(false), anyBoolean())).thenReturn(mockDefaultProjectColumnModels);
		when(viewEntity.getScopeIds()).thenReturn(mockViewScopeIds);
		when(mockColumnModelPage1.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		when(mockColumnModelPage1.getResults()).thenReturn(mockAnnotationColumnsPage1);
		when(mockColumnModelPage2.getNextPageToken()).thenReturn(null);
		when(mockColumnModelPage2.getResults()).thenReturn(mockAnnotationColumnsPage2);

		AsyncMockStubber.callSuccessWith(mockColumnModelPage1, mockColumnModelPage2).when(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigureTable() {
		widget.configure(tableEntity, TableType.table);
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(false);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(false);
		verify(mockEditor).configure(TableType.table, new ArrayList<ColumnModel>());
	}

	@Test
	public void testConfigureView() {
		verify(mockView).setJobTracker(any(Widget.class));
		verify(mockView).setEditor(any(Widget.class));

		widget.configure(viewEntity, TableType.files);
		verify(mockEditor).configure(TableType.files, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(true);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}

	@Test
	public void testConfigureProjectView() {
		verify(mockView).setJobTracker(any(Widget.class));
		verify(mockView).setEditor(any(Widget.class));

		widget.configure(viewEntity, TableType.projects);
		verify(mockEditor).configure(TableType.projects, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(true);
		verify(mockEditor).setAddAnnotationColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultProjectColumnModels);
	}

	@Test
	public void testGetPossibleColumnModelsForViewScope() {
		// test is set up so that rpc successfully returns 2 pages, and then stops.
		widget.configure(viewEntity, TableType.files);

		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(viewScopeCaptor.capture(), eq(firstPageToken), any(AsyncCallback.class));
		// verify scope
		ViewScope viewScope = viewScopeCaptor.getValue();
		assertEquals(mockViewScopeIds, viewScope.getScope());
		assertNull(viewScope.getViewType());
		assertEquals((Long) ViewTypeMask.File.getMask(), viewScope.getViewTypeMask());

		verify(mockEditor).addColumns(mockAnnotationColumnsPage1);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), eq(NEXT_PAGE_TOKEN), any(AsyncCallback.class));
		verify(mockEditor).addColumns(mockAnnotationColumnsPage2);
	}

	@Test
	public void testGetPossibleColumnModelsForProjectViewScope() {
		// test is set up so that rpc successfully returns 2 pages, and then stops.
		widget.configure(viewEntity, TableType.projects);

		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(viewScopeCaptor.capture(), eq(firstPageToken), any(AsyncCallback.class));
		// verify scope
		ViewScope viewScope = viewScopeCaptor.getValue();
		assertEquals(mockViewScopeIds, viewScope.getScope());
		assertNull(viewScope.getViewType());
		assertEquals((Long) ViewTypeMask.Project.getMask(), viewScope.getViewTypeMask());
	}

	@Test
	public void testGetPossibleColumnModelsForViewScopeFailure() {
		widget.configure(viewEntity, TableType.files);
		String error = "error message getting annotation column models";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), anyString(), any(AsyncCallback.class));
		String firstPageToken = null;
		widget.getPossibleColumnModelsForViewScope(firstPageToken);
		verify(mockSynapseClient).getPossibleColumnModelsForViewScope(any(ViewScope.class), eq(firstPageToken), any(AsyncCallback.class));
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
		widget.configure(tableEntity, TableType.table);

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
		widget.configure(tableEntity, TableType.table);

		// simulate user cancels
		wizardCallback.onCanceled();

		// verify attempt to clean up
		verify(mockJsClient).deleteEntityById(eq(ENTITY_ID), eq(true), any(AsyncCallback.class));
		verify(mockJsniUtils).consoleError(DELETE_PLACEHOLDER_FAILURE_MESSAGE + ENTITY_ID + ": " + errorMessage);
	}

	private AsynchronousProgressHandler onPrimary() {
		widget.configure(tableEntity, TableType.table);
		widget.onPrimary();
		boolean isDeterminate = false;
		ArgumentCaptor<AsynchronousProgressHandler> captor = ArgumentCaptor.forClass(AsynchronousProgressHandler.class);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(ColumnModelsWidget.UPDATING_SCHEMA), eq(isDeterminate), eq(AsynchType.TableTransaction), eq(mockTableSchemaChangeRequest), captor.capture());
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
		widget.configure(tableEntity, TableType.table);
		String error = "error message";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setError(ex);
	}
}
