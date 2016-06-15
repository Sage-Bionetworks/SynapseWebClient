package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;



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
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
	
		widget = new CreateTableViewWizardStep2(mockEditor, mockSynapseClient);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		when(mockEditor.validate()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).setTableSchema(any(Table.class), anyList(), any(AsyncCallback.class));
	}
	
	@Test
	public void testConfigureTable(){
		widget.configure(tableEntity, TableType.table);
		verify(mockEditor).setAddDefaultViewColumnsButtonVisible(false);
		verify(mockEditor).configure(new ArrayList<ColumnModel>());
	}
	
	@Test
	public void testConfigureView() {
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
	
	@Test
	public void testOnPrimary(){
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).onFinished();
	}
	
	@Test
	public void testOnPrimaryFailure(){
		String error = "error message";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).setTableSchema(any(Table.class), anyList(), any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setErrorMessage(error);
	}
}
