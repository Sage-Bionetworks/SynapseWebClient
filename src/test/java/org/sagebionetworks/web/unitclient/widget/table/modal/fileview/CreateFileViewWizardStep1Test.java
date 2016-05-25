package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.FileView;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateFileViewWizardStep1;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateFileViewWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import static org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidgetImpl.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateFileViewWizardStep1Test {

	@Mock
	CreateFileViewWizardStep1View mockView;
	@Mock
	ModalPresenter mockWizardPresenter;
	SynapseClientAsync mockSynapseClient;
	String parentId;
	CreateFileViewWizardStep1 widget;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		
		widget = new CreateFileViewWizardStep1(mockView, mockSynapseClient);
		widget.setModalPresenter(mockWizardPresenter);
		parentId = "syn123";
		widget.configure(parentId);
	}
	
	@Test
	public void testNullName(){
		when(mockView.getName()).thenReturn(null);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(CreateFileViewWizardStep1.TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).createEntity(any(TableEntity.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateHappy(){
		String tableName = "a name";
		FileView table = new FileView();
		table.setName(tableName);
		table.setId("syn57");
		AsyncMockStubber.callSuccessWith(table).when(mockSynapseClient).createEntity(any(TableEntity.class), any(AsyncCallback.class));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		verify(mockWizardPresenter, never()).setErrorMessage(anyString());
		
		//TODO: go to next step
		verify(mockWizardPresenter).onFinished();
	}
	
	@Test
	public void testCreateFailed(){
		String tableName = "a name";
		FileView table = new FileView();
		table.setName(tableName);
		table.setId("syn57");
		String error = "name already exists";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).createEntity(any(TableEntity.class), any(AsyncCallback.class));
		when(mockView.getName()).thenReturn(tableName);
		widget.onPrimary();
		verify(mockWizardPresenter).setErrorMessage(error);
		verify(mockWizardPresenter).setLoading(false);

		//TODO: should not go to the next step
		verify(mockWizardPresenter, never()).onFinished();

	}
	
}
