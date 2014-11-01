package org.sagebionetworks.web.unitclient.widget.table.modal;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalView;
import org.sagebionetworks.web.client.widget.table.modal.CreateTableModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateTableModalWidgetTest {

	CreateTableModalView mockView;
	SynapseClientAsync mockSynapseClient;
	TableCreatedHandler mockHandler;
	
	String parentId;
	CreateTableModalWidgetImpl widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(CreateTableModalView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockHandler = Mockito.mock(TableCreatedHandler.class);
		widget = new CreateTableModalWidgetImpl(mockView, mockSynapseClient);
		parentId = "syn123";
		widget.configure(parentId, mockHandler);
	}
	
	@Test
	public void testShowCreateModal(){
		widget.showCreateModal();
		verify(mockView).clear();
		verify(mockView).show();
	}
	
	@Test
	public void testNullName(){
		widget.showCreateModal();
		when(mockView.getTableName()).thenReturn(null);
		widget.onCreateTable();
		verify(mockView).showError(CreateTableModalWidgetImpl.TABLE_NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
		verify(mockSynapseClient, never()).createTableEntity(any(TableEntity.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testCreateHappy(){
		String tableName = "a name";
		TableEntity table = new TableEntity();
		table.setName(tableName);
		table.setId("syn57");
		AsyncMockStubber.callSuccessWith(table).when(mockSynapseClient).createTableEntity(any(TableEntity.class), any(AsyncCallback.class));
		widget.showCreateModal();
		when(mockView.getTableName()).thenReturn(tableName);
		widget.onCreateTable();
		verify(mockView, never()).showError(anyString());
		verify(mockView).hide();
		verify(mockHandler).tableCreated();
	}
	
	@Test
	public void testCreateFailed(){
		String tableName = "a name";
		TableEntity table = new TableEntity();
		table.setName(tableName);
		table.setId("syn57");
		String error = "name already exists";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseClient).createTableEntity(any(TableEntity.class), any(AsyncCallback.class));
		widget.showCreateModal();
		when(mockView.getTableName()).thenReturn(tableName);
		widget.onCreateTable();
		verify(mockView).showError(error);
		verify(mockView).setLoading(false);
		// Should not hide with error.
		verify(mockView, never()).hide();
		verify(mockHandler, never()).tableCreated();
	}
	
}
