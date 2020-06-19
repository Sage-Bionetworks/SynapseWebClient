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
	// unable to mock these column models since we use the json adapter to deep clone the objects
	ColumnModel fileColumn;
	ColumnModel projectColumn;
	ColumnModel submissionViewColumn;
	public static final String FILE_COLUMN = "default file column name";
	public static final String PROJECT_COLUMN = "default project name";
	public static final String SUBMISSION_VIEW_COLUMN = "default submission view column name";
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(mockException.getMessage()).thenReturn(errorMessage);
		fileColumn = new ColumnModel();
		fileColumn.setId("1");
		fileColumn.setName(FILE_COLUMN);
		projectColumn = new ColumnModel();
		projectColumn.setId("2");
		projectColumn.setName(PROJECT_COLUMN);
		submissionViewColumn = new ColumnModel();
		submissionViewColumn.setId("3");
		submissionViewColumn.setName(SUBMISSION_VIEW_COLUMN);
		
		adapterFactory = new AdapterFactoryImpl();
		columns = Collections.singletonList(fileColumn);
		projectColumns = Collections.singletonList(projectColumn);
		submissionViewColumns = Collections.singletonList(submissionViewColumn);
		when(mockJsClient.getDefaultColumnsForView(TableType.files.getViewTypeMask())).thenReturn(getDoneFuture(columns));
		when(mockJsClient.getDefaultColumnsForView(TableType.projects.getViewTypeMask())).thenReturn(getDoneFuture(projectColumns));
		when(mockJsClient.getDefaultColumnsForView(ViewEntityType.submissionview)).thenReturn(getDoneFuture(submissionViewColumns));		
		fileViewDefaultColumns = new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);
	}

	@Test
	public void testGetDefaultColumnNames() {
		Set<String> columnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.files);
		Set<String> projectColumnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.projects);
		Set<String> submissionViewColumnNames = fileViewDefaultColumns.getDefaultViewColumnNames(TableType.submission_view);
		assertTrue(columnNames.contains(FILE_COLUMN));
		assertTrue(projectColumnNames.contains(PROJECT_COLUMN));
		assertTrue(submissionViewColumnNames.contains(SUBMISSION_VIEW_COLUMN));
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
