package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.table.v2.schema.ImportTableViewColumnsButton;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImportTableViewColumnsButtonTest {
	ImportTableViewColumnsButton widget;
	
	@Mock
	Button mockButton;
	@Mock
	EntityFinder mockFinder;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	@Mock
	EntityBundle mockBundle;
	@Mock
	Project mockProject;
	@Mock
	Table mockTable;
	@Mock
	TableBundle mockTableBundle;
	@Mock
	ColumnModel mockTableColumnModel;
	List<ColumnModel> tableColumnModels;
	@Mock
	CallbackP<List<ColumnModel>> mockCallback;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new ImportTableViewColumnsButton(mockButton, mockFinder, mockSynapseClient);
		AsyncMockStubber.callSuccessWith(mockBundle).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		when(mockBundle.getEntity()).thenReturn(mockTable);
		when(mockBundle.getTableBundle()).thenReturn(mockTableBundle);
		tableColumnModels = Collections.singletonList(mockTableColumnModel);
		when(mockTableBundle.getColumnModels()).thenReturn(tableColumnModels);
	}
	
	@Test
	public void testConstruction() {
		verify(mockButton).setText(ImportTableViewColumnsButton.BUTTON_TEXT);
		verify(mockButton).setSize(ButtonSize.DEFAULT);
		verify(mockButton).setIcon(IconType.PLUS);
		verify(mockButton).setType(ButtonType.DEFAULT);
	}
	
	@Test
	public void testOnClick() {
		verify(mockButton).addClickHandler(clickHandlerCaptor.capture());
		//simulate click
		clickHandlerCaptor.getValue().onClick(null);
		verify(mockFinder).show();
	}
	
	@Test
	public void testOnTableSelected() {
		String entityId = "syn100000000";
		widget.configure(mockCallback);
		
		widget.onTableViewSelected(entityId);
		
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockFinder).hide();
		verify(mockTableColumnModel).setId(null);
		verify(mockCallback).invoke(tableColumnModels);
	}
	
	@Test
	public void testOnProjectSelected() {
		String entityId = "syn100000000";
		widget.configure(mockCallback);
		when(mockBundle.getEntity()).thenReturn(mockProject);
		
		widget.onTableViewSelected(entityId);
		
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockFinder).showError(anyString());
		verify(mockFinder, never()).hide();
		verify(mockCallback, never()).invoke(anyList());
	}
	
	@Test
	public void testOnGetEntityError() {
		String error = "problem getting entity";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		String entityId = "syn100000000";
		widget.configure(mockCallback);
		
		widget.onTableViewSelected(entityId);
		
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockFinder).showError(eq(error));
		verify(mockFinder, never()).hide();
		verify(mockCallback, never()).invoke(anyList());
	}
}
