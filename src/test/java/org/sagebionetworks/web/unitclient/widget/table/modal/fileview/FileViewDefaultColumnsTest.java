package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileViewDefaultColumnsTest {
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	ColumnModel columnModel;
	List<ColumnModel> columns;
	ViewDefaultColumns fileViewDefaultColumns;
	@Mock
	Exception mockException;
	@Mock
	PopupUtilsView mockPopupUtils;
	AdapterFactoryImpl adapterFactory;
	public static final String errorMessage = "an error occurred.";
	
	@Captor
	ArgumentCaptor<Set<String>> setCaptor;	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockException.getMessage()).thenReturn(errorMessage);
		columnModel = new ColumnModel();
		columnModel.setId("not null");
		adapterFactory = new AdapterFactoryImpl();
		columns = Collections.singletonList(columnModel);
		AsyncMockStubber.callSuccessWith(columns).when(mockSynapseClient).getDefaultColumnsForView(any(ViewType.class), any(AsyncCallback.class));
		fileViewDefaultColumns = new ViewDefaultColumns(mockSynapseClient, adapterFactory, mockPopupUtils);
	}

	@Test
	public void testGetDefaultColumnsWithIds() {
		boolean isClearIds = false;
		assertEquals(columns, fileViewDefaultColumns.getDefaultFileViewColumns(isClearIds));
		
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.project), any(AsyncCallback.class));
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		
		//verify results are cached
		fileViewDefaultColumns.getDefaultFileViewColumns(isClearIds);
		verifyNoMoreInteractions(mockSynapseClient);
	}
	
	@Test
	public void testGetDefaultColumnNames() {
		String colName = "default column name";
		columnModel.setName(colName);
		Set<String> columnNames = fileViewDefaultColumns.getDefaultFileViewColumnNames();
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.project), any(AsyncCallback.class));
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		assertTrue(columnNames.contains(colName));
		
		//verify results are cached
		fileViewDefaultColumns.getDefaultFileViewColumnNames();
		verifyNoMoreInteractions(mockSynapseClient);
	}
	
	@Test
	public void testInitFailureFailure() {
		AsyncMockStubber.callFailureWith(mockException).when(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		fileViewDefaultColumns = new ViewDefaultColumns(mockSynapseClient, adapterFactory, mockPopupUtils);
		
		verify(mockSynapseClient, times(2)).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}
	
	@Test
	public void testProjectInitFailure() {
		AsyncMockStubber.callFailureWith(mockException).when(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.project), any(AsyncCallback.class));
		fileViewDefaultColumns = new ViewDefaultColumns(mockSynapseClient, adapterFactory, mockPopupUtils);
		
		verify(mockSynapseClient, times(2)).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}


}
