package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.utils.FutureUtils.*;

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
import org.sagebionetworks.web.client.SynapseFutureClient;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ViewDefaultColumnsTest {
	
	@Mock
	SynapseFutureClient mockFutureClient;
	ColumnModel columnModel;
	List<ColumnModel> columns, projectColumns, fileAndTableColumns;
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
		projectColumns = Collections.singletonList(columnModel);
		fileAndTableColumns = Collections.singletonList(columnModel);
		when(mockFutureClient.getDefaultColumnsForView(eq(ViewType.file))).thenReturn(getDoneFuture(columns));
		when(mockFutureClient.getDefaultColumnsForView(eq(ViewType.project))).thenReturn(getDoneFuture(projectColumns));
		when(mockFutureClient.getDefaultColumnsForView(eq(ViewType.file_and_table))).thenReturn(getDoneFuture(fileAndTableColumns));
		fileViewDefaultColumns = new ViewDefaultColumns(mockFutureClient, adapterFactory, mockPopupUtils);
	}

	@Test
	public void testGetDefaultColumnsWithIds() {
		boolean isClearIds = false;
		assertEquals(columns, fileViewDefaultColumns.getDefaultViewColumns(ViewType.file, isClearIds));
		assertEquals(projectColumns, fileViewDefaultColumns.getDefaultViewColumns(ViewType.project,isClearIds));
	}
	
	@Test
	public void testGetDefaultColumnNames() {
		String colName = "default column name";
		columnModel.setName(colName);
		Set<String> columnNames = fileViewDefaultColumns.getDefaultFileViewColumnNames();
		Set<String> projectColumnNames = fileViewDefaultColumns.getDefaultProjectViewColumnNames();
		assertTrue(columnNames.contains(colName));
		assertTrue(projectColumnNames.contains(colName));
	}
	
	@Test
	public void testInitFailureFailure() {
		when(mockFutureClient.getDefaultColumnsForView(eq(ViewType.file))).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockFutureClient, adapterFactory, mockPopupUtils);
		
		verify(mockFutureClient, times(2)).getDefaultColumnsForView(eq(ViewType.file));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}
	
	@Test
	public void testProjectInitFailure() {
		when(mockFutureClient.getDefaultColumnsForView(eq(ViewType.project))).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockFutureClient, adapterFactory, mockPopupUtils);
		
		verify(mockFutureClient, times(2)).getDefaultColumnsForView(eq(ViewType.project));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}


}
