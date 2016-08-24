package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableSchemaChangeRequest;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionRequest;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
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
	EntityUpdatedHandler mockUpdateHandler;
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
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, mockEditor, mockJobTrackingWidget);
		when(mockEditor.validate()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockTableSchemaChangeRequest).when(mockSynapseClient).getTableUpdateTransactionRequest(anyString(), anyList(), anyList(), any(AsyncCallback.class));
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
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
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
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(true);
	}
	
	@Test
	public void testGetDefaultColumnsForView() {
		boolean isEditable = true;
		AsyncMockStubber.callSuccessWith(mockDefaultColumnModels).when(mockSynapseClient).getDefaultColumnsForView(any(org.sagebionetworks.repo.model.table.ViewType.class), any(AsyncCallback.class));
		when(mockBundle.getEntity()).thenReturn(mockView);
		tableBundle.setColumnModels(TableModelTestUtils.createOneOfEachType(true));
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		widget.getDefaultColumnsForView();
		verify(mockEditor).addColumns(mockDefaultColumnModels);
	}
	
	@Test
	public void testConfigureViewFailure() {
		boolean isEditable = true;
		String error = "error message getting default column models";
		Exception ex = new Exception(error);
		when(mockBundle.getEntity()).thenReturn(mockView);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getDefaultColumnsForView(any(org.sagebionetworks.repo.model.table.ViewType.class), any(AsyncCallback.class));
		tableBundle.setColumnModels(TableModelTestUtils.createOneOfEachType(true));
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		widget.getDefaultColumnsForView();
		verify(mockBaseView).hideErrors();
		verify(mockBaseView).showError(error);
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor).configure(schema);
		verify(mockBaseView).showEditor();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEditable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// should fail
		widget.onEditColumns();
	}
	
	private AsynchronousProgressHandler onSave() {
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
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
		// Save success should be called.
		verify(mockUpdateHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
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
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
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
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
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
	
	
}
