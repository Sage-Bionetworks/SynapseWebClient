package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizardStep2;
import org.sagebionetworks.web.client.widget.table.modal.fileview.EntityContainerListWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;



public class CreateTableViewWizardStep1Test {

	@Mock
	CreateTableViewWizardStep1View mockView;
	@Mock
	ModalPresenter mockWizardPresenter;
	@Mock
	EntityContainerListWidget mockEntityContainerListWidget;
	@Mock
	CreateTableViewWizardStep2 mockStep2;
	
	SynapseClientAsync mockSynapseClient;
	String parentId;
	CreateTableViewWizardStep1 widget;
	List<String> scopeIds;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		scopeIds = Collections.singletonList("3");
		when(mockEntityContainerListWidget.getEntityIds()).thenReturn(scopeIds);
		widget = new CreateTableViewWizardStep1(mockView, mockSynapseClient, mockEntityContainerListWidget, mockStep2);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
	}
	
	@Test
	public void testNullName(){
		widget.configure(parentId, TableType.view);
		when(mockView.getName()).thenReturn(null);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(CreateTableViewWizardStep1.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).createEntity(any(Entity.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateFileView(){
		widget.configure(parentId, TableType.view);
		verify(mockView).setName("");
		verify(mockView).setScopeWidgetVisible(true);
		String tableName = "a name";
		FileView table = new FileView();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		AsyncMockStubber.callSuccessWith(table).when(mockSynapseClient).createEntity(captor.capture(), any(AsyncCallback.class));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		FileView capturedFileView = (FileView)captor.getValue();
		assertEquals(scopeIds, capturedFileView.getScopeIds());
		assertEquals(FileView.class.getName(), capturedFileView.getEntityType());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.view);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);
	}
	
	@Test
	public void testCreateTable(){
		widget.configure(parentId, TableType.table);
		verify(mockView).setScopeWidgetVisible(false);
		String tableName = "a name";
		TableEntity table = new TableEntity();
		table.setName(tableName);
		table.setId("syn57");
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		AsyncMockStubber.callSuccessWith(table).when(mockSynapseClient).createEntity(captor.capture(), any(AsyncCallback.class));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		TableEntity capturedTable = (TableEntity)captor.getValue();
		assertEquals(TableEntity.class.getName(), capturedTable.getEntityType());
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		verify(mockStep2).configure(table, TableType.table);
		verify(mockWizardPresenter).setNextActivePage(mockStep2);;
	}
	
	@Test
	public void testCreateFailed(){
		widget.configure(parentId, TableType.view);
		String tableName = "a name";
		String error = "name already exists";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).createEntity(any(Entity.class), any(AsyncCallback.class));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(error);

		//TODO: should not go to the next step
		verify(mockWizardPresenter, never()).onFinished();
	}
	
}
