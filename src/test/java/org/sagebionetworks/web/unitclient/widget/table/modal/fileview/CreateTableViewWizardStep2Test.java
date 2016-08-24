package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableSchemaChangeRequest;
import org.sagebionetworks.repo.model.table.TableUpdateRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
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
	ModalPresenter mockWizardPresenter;
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
	JobTrackingWidget mockJobTrackingWidget;
	@Mock
	TableUpdateTransactionRequest mockTableSchemaChangeRequest;
	@Mock
	CreateTableViewWizardStep2View mockView;
	@Mock
	TableUpdateRequest mockTableUpdateRequest;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
	
		widget = new CreateTableViewWizardStep2(mockView, mockEditor, mockSynapseClient, mockJobTrackingWidget);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		when(mockEditor.validate()).thenReturn(true);
		when(mockTableSchemaChangeRequest.getChanges()).thenReturn(Collections.singletonList(mockTableUpdateRequest));
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureTable(){
		widget.configure(tableEntity, TableType.table);
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(false);
		verify(mockEditor).configure(new ArrayList<ColumnModel>());
	}
	
	@Test
	public void testConfigureView() {
		verify(mockView).setJobTracker(any(Widget.class));
		verify(mockView).setEditor(any(Widget.class));
		
		AsyncMockStubber.callSuccessWith(mockDefaultColumnModels).when(mockSynapseClient).getDefaultColumnsForView(any(ViewType.class), any(AsyncCallback.class));
		widget.configure(viewEntity, TableType.view);
		verify(mockEditor).configure(new ArrayList<ColumnModel>());
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(true);
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}
	
	@Test
	public void testConfigureViewFailure() {
		String error = "error message getting default column models";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getDefaultColumnsForView(any(ViewType.class), any(AsyncCallback.class));
		widget.configure(viewEntity, TableType.view);
		verify(mockWizardPresenter).setErrorMessage(error);
	}

	@Test
	public void testAsWidget(){
		widget.asWidget();
		verify(mockEditor).asWidget();
	}
	

	@Test
	public void testOnPrimaryInvalid(){
		when(mockEditor.validate()).thenReturn(false);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
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
	public void testOnPrimary(){
		onPrimary().onComplete(null);
		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).onFinished();
	}
	
	@Test
	public void testOnPrimaryAsyncCancelled(){
		onPrimary().onCancel();
		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setErrorMessage(CreateTableViewWizardStep2.SCHEMA_UPDATE_CANCELLED);
	}
	
	@Test
	public void testOnPrimaryAsyncFailure(){
		String errorMessage = "error during schema update";
		Exception ex = new Exception(errorMessage);
		onPrimary().onFailure(ex);
		verify(mockWizardPresenter, atLeastOnce()).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setErrorMessage(errorMessage);
	}
	
	
	@Test
	public void testOnPrimaryFailure(){
		widget.configure(tableEntity, TableType.table);
		String error = "error message";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setErrorMessage(error);
	}
}
