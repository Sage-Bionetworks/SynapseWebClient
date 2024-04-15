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

  List<
    ColumnModel
  > columns, projectColumns, submissionViewColumns, datasetColumns, datasetCollectionColumns;
  ViewDefaultColumns viewDefaultColumns;

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
  ColumnModel datasetColumn;
  ColumnModel datasetCollectionColumn;
  public static final String FILE_COLUMN = "default file column name";
  public static final String PROJECT_COLUMN = "default project name";
  public static final String SUBMISSION_VIEW_COLUMN =
    "default submission view column name";
  public static final String DATASET_COLUMN = "default dataset column name";
  public static final String DATASET_COLLECTION_COLUMN =
    "default dataset collection column name";

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
    datasetColumn = new ColumnModel();
    datasetColumn.setId("4");
    datasetColumn.setName(DATASET_COLUMN);
    datasetCollectionColumn = new ColumnModel();
    datasetCollectionColumn.setId("5");
    datasetCollectionColumn.setName(DATASET_COLLECTION_COLUMN);

    adapterFactory = new AdapterFactoryImpl();
    columns = Collections.singletonList(fileColumn);
    projectColumns = Collections.singletonList(projectColumn);
    submissionViewColumns = Collections.singletonList(submissionViewColumn);
    datasetColumns = Collections.singletonList(datasetColumn);
    datasetCollectionColumns =
      Collections.singletonList(datasetCollectionColumn);
    when(
      mockJsClient.getDefaultColumnsForView(
        TableType.file_view.getViewTypeMask()
      )
    )
      .thenReturn(getDoneFuture(columns));
    when(
      mockJsClient.getDefaultColumnsForView(
        TableType.project_view.getViewTypeMask()
      )
    )
      .thenReturn(getDoneFuture(projectColumns));
    when(mockJsClient.getDefaultColumnsForView(ViewEntityType.submissionview))
      .thenReturn(getDoneFuture(submissionViewColumns));
    when(mockJsClient.getDefaultColumnsForView(ViewEntityType.dataset))
      .thenReturn(getDoneFuture(datasetColumns));
    when(
      mockJsClient.getDefaultColumnsForView(ViewEntityType.datasetcollection)
    )
      .thenReturn(getDoneFuture(datasetCollectionColumns));
    viewDefaultColumns =
      new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);
  }

  @Test
  public void testGetDefaultColumnNames() {
    Set<String> columnNames = viewDefaultColumns.getDefaultViewColumnNames(
      TableType.file_view
    );
    Set<String> projectColumnNames =
      viewDefaultColumns.getDefaultViewColumnNames(TableType.project_view);
    Set<String> submissionViewColumnNames =
      viewDefaultColumns.getDefaultViewColumnNames(TableType.submission_view);
    Set<String> datasetColumnNames =
      viewDefaultColumns.getDefaultViewColumnNames(TableType.dataset);
    Set<String> datasetCollectionColumnNames =
      viewDefaultColumns.getDefaultViewColumnNames(
        TableType.dataset_collection
      );
    assertTrue(columnNames.contains(FILE_COLUMN));
    assertTrue(projectColumnNames.contains(PROJECT_COLUMN));
    assertTrue(submissionViewColumnNames.contains(SUBMISSION_VIEW_COLUMN));
    assertTrue(datasetColumnNames.contains(DATASET_COLUMN));
    assertTrue(
      datasetCollectionColumnNames.contains(DATASET_COLLECTION_COLUMN)
    );
  }

  @Test
  public void testInitFailureFailure() {
    when(
      mockJsClient.getDefaultColumnsForView(
        TableType.file_view.getViewTypeMask()
      )
    )
      .thenReturn(getFailedFuture(mockException));
    viewDefaultColumns =
      new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

    verify(mockJsClient, times(2))
      .getDefaultColumnsForView(TableType.file_view.getViewTypeMask());
    verify(mockPopupUtils).showErrorMessage(errorMessage);
  }

  @Test
  public void testProjectInitFailure() {
    when(
      mockJsClient.getDefaultColumnsForView(
        TableType.project_view.getViewTypeMask()
      )
    )
      .thenReturn(getFailedFuture(mockException));
    viewDefaultColumns =
      new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

    verify(mockJsClient, times(2))
      .getDefaultColumnsForView(TableType.project_view.getViewTypeMask());
    verify(mockPopupUtils).showErrorMessage(errorMessage);
  }

  @Test
  public void testSubmissionViewInitFailure() {
    when(mockJsClient.getDefaultColumnsForView(ViewEntityType.submissionview))
      .thenReturn(getFailedFuture(mockException));
    viewDefaultColumns =
      new ViewDefaultColumns(mockJsClient, adapterFactory, mockPopupUtils);

    verify(mockJsClient, times(2))
      .getDefaultColumnsForView(ViewEntityType.submissionview);
    verify(mockPopupUtils).showErrorMessage(errorMessage);
  }
}
