package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.assertTrue;
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
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.modal.fileview.ViewDefaultColumns;

public class ViewDefaultColumnsTest {

	@Mock
	SynapseJavascriptClient mockJsClient;
	ColumnModel columnModel;
	List<ColumnModel> columns, projectColumns, submissionViewColumns;
	ViewDefaultColumns fileViewDefaultColumns;
	@Mock
	Exception mockException;
	@Mock
	PopupUtilsView mockPopupUtils;
	AdapterFactoryImpl adapterFactory;
	public static final String errorMessage = "an error occurred.";

	@Captor
	ArgumentCaptor<Set<String>> setCaptor;
	public static final String colName = "default column name";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockException.getMessage()).thenReturn(errorMessage);
		columnModel = new ColumnModel();
		columnModel.setId("not null");
		columnModel.setName(colName);
		adapterFactory = new AdapterFactoryImpl();
		columns = Collections.singletonList(columnModel);
		projectColumns = Collections.singletonList(columnModel);
		submissionViewColumns = Collections.singletonList(columnModel);
		when(mockJsClient.getDefaultColumnsForView(TableType.files.getViewTypeMask())).thenReturn(getDoneFuture(columns));
		when(mockJsClient.getDefaultColumnsForView(TableType.projects.getViewTypeMask())).thenReturn(getDoneFuture(projectColumns));
		when(mockJsClient.getDefaultColumnsForView(ViewEntityType.submissionview)).thenReturn(getDoneFuture(submissionViewColumns));		
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);
	}

	@Test
	public void testGetDefaultColumnNames() {
		columnModel.setName(colName);
		Set<String> columnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.files);
		Set<String> projectColumnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.projects);
		Set<String> submissionViewColumnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.submission_view);
		assertTrue(columnNames.contains(colName));
		assertTrue(projectColumnNames.contains(colName));
		assertTrue(submissionViewColumnNames.contains(colName));
	}

	@Test
	public void testInitFailureFailure() {
		when(mockJsClient.getDefaultColumnsForView(TableType.files.getViewTypeMask())).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

		verify(mockJsClient, times(2)).getDefaultColumnsForView(TableType.files.getViewTypeMask());
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}

	@Test
	public void testProjectInitFailure() {
		when(mockJsClient.getDefaultColumnsForView(TableType.projects.getViewTypeMask())).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

		verify(mockJsClient, times(2)).getDefaultColumnsForView(TableType.projects.getViewTypeMask());
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}

	@Test
	public void testSubmissionViewInitFailure() {
		when(mockJsClient.getDefaultColumnsForView(ViewEntityType.submissionview)).thenReturn(getFailedFuture(mockException));
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

		verify(mockJsClient, times(2)).getDefaultColumnsForView(ViewEntityType.submissionview);
		verify(mockPopupUtils).showErrorMessage(errorMessage);
	}


}
