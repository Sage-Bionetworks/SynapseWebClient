package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;
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
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;

public class ViewDefaultColumnsTest {

	@Mock
	SynapseJavascriptClient mockJsClient;
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
	ArgumentCaptor<Set<String>> setCaptor;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockException.getMessage()).thenReturn(errorMessage);
		columnModel = new ColumnModel();
		columnModel.setId("not null");
		adapterFactory = new AdapterFactoryImpl();
		columns = Collections.singletonList(columnModel);
		projectColumns = Collections.singletonList(columnModel);
		fileAndTableColumns = Collections.singletonList(columnModel);
		when(mockJsClient.getDefaultColumnsForView(eq(ViewType.file))).thenReturn(getDoneFuture(columns));
		when(mockJsClient.getDefaultColumnsForView(eq(ViewType.project))).thenReturn(getDoneFuture(projectColumns));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);
	}

	@Test
	public void testGetDefaultColumnsWithIds() {
		boolean isClearIds = false;
		assertEquals(columns, fileViewDefaultColumns.getDefaultViewColumns(true, isClearIds));
		assertEquals(projectColumns, fileViewDefaultColumns.getDefaultViewColumns(false, isClearIds));
	}

	@Test
	public void testGetDefaultColumnNames() {
		String colName = "default column name";
		columnModel.setName(colName);
		Set<String> columnNames = fileViewDefaultColumns.getDefaultViewColumnNames(true);
		Set<String> projectColumnNames = fileViewDefaultColumns.getDefaultViewColumnNames(false);
		assertTrue(columnNames.contains(colName));
		assertTrue(projectColumnNames.contains(colName));
	}

	@Test
	public void testInitFailureFailure() {
		when(mockJsClient.getDefaultColumnsForView(eq(ViewType.file))).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

		verify(mockJsClient, times(2)).getDefaultColumnsForView(eq(ViewType.file));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}

	@Test
	public void testProjectInitFailure() {
		when(mockJsClient.getDefaultColumnsForView(eq(ViewType.project))).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

		verify(mockJsClient, times(2)).getDefaultColumnsForView(eq(ViewType.project));
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}


}
