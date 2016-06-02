package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;
import org.sagebionetworks.web.unitclient.widget.table.v2.schema.ColumnModelTableRowEditorStub;
import org.sagebionetworks.web.unitclient.widget.table.v2.schema.ColumnModelTableRowViewerStub;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sun.jersey.spi.inject.Errors.ErrorMessage;



public class CreateTableViewWizardStep2Test {

	@Mock
	ColumnModelsEditorWidget mockEditor;
	@Mock
	ModalPresenter mockWizardPresenter;
	String parentId;
	CreateTableViewWizardStep2 widget;
	@Mock
	FileView viewEntity;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
	
		widget = new CreateTableViewWizardStep2(mockEditor);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		when(mockEditor.validate()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(null).when(mockEditor).setTableSchema(any(AsyncCallback.class));
	}
	
	@Test
	public void testConstruction(){
		verify(mockEditor).setAddAllAnnotationsButtonVisible(false);
		verify(mockEditor).setAddDefaultFileColumnsButtonVisible(false);
	}
	
	@Test
	public void testConfigure(){
		widget.configure(viewEntity, TableType.view);
		verify(mockEditor).configure(viewEntity, new ArrayList<ColumnModel>());
		verify(mockEditor).setAddAllAnnotationsButtonVisible(true);
		verify(mockEditor).setAddDefaultFileColumnsButtonVisible(true);
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
		verify(mockWizardPresenter).setLoading(false);
	}
	
	@Test
	public void testOnPrimary(){
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setLoading(false);
		verify(mockWizardPresenter).onFinished();
	}
	
	@Test
	public void testOnPrimaryFailure(){
		String error = "error message";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockEditor).setTableSchema(any(AsyncCallback.class));
		widget.onPrimary();
		verify(mockWizardPresenter).setLoading(true);
		verify(mockEditor).validate();
		verify(mockWizardPresenter).setLoading(false);
		verify(mockWizardPresenter).setErrorMessage(error);
	}


	
	
}
