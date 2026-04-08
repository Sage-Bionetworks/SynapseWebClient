package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.jsinterop.ColumnModelJsObject;
import org.sagebionetworks.web.client.jsinterop.CsvPreviewProps;
import org.sagebionetworks.web.client.jsinterop.UploadToTablePreviewResultJsObject;
import org.sagebionetworks.web.client.widget.CsvPreview;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadRequestUtils;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;

public class UploadCSVPreviewPageImplTest {

  UploadCSVAppendPage mockAppendNextPage;
  UploadCSVFinishPage mockCreateNextPage;
  UploadCSVPreviewPageView mockView;
  CSVOptionsWidget mockCSVOptionsWidget;
  CsvPreview mockCsvPreview;
  ModalPresenter mockPresenter;
  ContentTypeDelimiter type;
  String fileName;
  String parentId;
  String fileHandleId;
  String tableId;
  UploadToTablePreviewRequest previewRequest;
  UploadToTableRequest uploadRequest;
  ColumnModel column;
  List<ColumnModel> schema;
  UploadCSVPreviewPageImpl page;

  @Before
  public void before() {
    mockView = Mockito.mock(UploadCSVPreviewPageView.class);
    mockCreateNextPage = Mockito.mock(UploadCSVFinishPage.class);
    mockAppendNextPage = Mockito.mock(UploadCSVAppendPage.class);
    mockCSVOptionsWidget = Mockito.mock(CSVOptionsWidget.class);
    mockCsvPreview = Mockito.mock(CsvPreview.class);

    mockPresenter = Mockito.mock(ModalPresenter.class);
    page =
      new UploadCSVPreviewPageImpl(
        mockView,
        mockCSVOptionsWidget,
        mockCsvPreview,
        mockCreateNextPage,
        mockAppendNextPage
      );
    type = ContentTypeDelimiter.CSV;
    fileName = "testing.csv";
    parentId = "syn123";
    fileHandleId = "456";
    tableId = "987654";
    column = new ColumnModel();
    column.setId("007");
    column.setColumnType(ColumnType.STRING);
    previewRequest = new UploadToTablePreviewRequest();
    previewRequest.setUploadFileHandleId(fileHandleId);
    CsvTableDescriptor csvTableDescriptor = new CsvTableDescriptor();
    csvTableDescriptor.setSeparator(type.getDelimiter());
    previewRequest.setCsvTableDescriptor(csvTableDescriptor);
    uploadRequest = UploadRequestUtils.createFromPreview(previewRequest);
    schema = Arrays.asList(column);
    when(mockCSVOptionsWidget.getCurrentOptions()).thenReturn(previewRequest);
  }

  // Capture the data callback passed to CsvPreview.configure(...)
  // and invoke it with a minimal preview payload.
  private void fireOnCsvPreviewDataChange(ColumnModel column) {
    ArgumentCaptor<CsvPreviewProps.OnCsvPreviewDataChangeFunction> dataCaptor =
      ArgumentCaptor.forClass(
        CsvPreviewProps.OnCsvPreviewDataChangeFunction.class
      );

    verify(mockCsvPreview)
      .configure(
        eq(fileHandleId),
        any(CsvTableDescriptor.class),
        dataCaptor.capture(),
        any()
      );

    // Capture the data callback that UploadCSVPreviewPageImpl passes to the React wrapper.
    UploadToTablePreviewResultJsObject data =
      new UploadToTablePreviewResultJsObject();

    ColumnModelJsObject jsColumn = new ColumnModelJsObject();

    jsColumn.id = column.getId();
    jsColumn.name = column.getName();
    jsColumn.columnType = column.getColumnType().name();

    data.suggestedColumns = new ColumnModelJsObject[] { jsColumn };

    // Simulate React sending preview results back into the presenter.
    dataCaptor.getValue().onCsvPreviewDataChange(data);
  }

  @Test
  public void testSetModalPresenter_configuresCsvPreview() {
    page.configure(type, fileName, parentId, fileHandleId, null);
    page.setModalPresenter(mockPresenter);

    verify(mockPresenter).setPrimaryButtonText(UploadCSVPreviewPageImpl.NEXT);
    verify(mockCsvPreview)
      .configure(eq(fileHandleId), any(CsvTableDescriptor.class), any(), any());
  }

  // Verify the onIsLoadingChange callback passed to the React component correctly
  // forwards loading state changes to the GWT presenter
  @Test
  public void testSetModalPresenter_isLoadingCallback_updatesPresenter() {
    page.configure(type, fileName, parentId, fileHandleId, null);
    page.setModalPresenter(mockPresenter);

    ArgumentCaptor<CsvPreviewProps.OnIsLoadingChangeFunction> loadingCaptor =
      ArgumentCaptor.forClass(CsvPreviewProps.OnIsLoadingChangeFunction.class);
    verify(mockCsvPreview)
      .configure(
        eq(fileHandleId),
        any(CsvTableDescriptor.class),
        any(),
        loadingCaptor.capture()
      );

    CsvPreviewProps.OnIsLoadingChangeFunction loadingCallback =
      loadingCaptor.getValue();

    loadingCallback.onIsLoadingChange(true);
    verify(mockPresenter).setLoading(true);

    loadingCallback.onIsLoadingChange(false);
    verify(mockPresenter).setLoading(false);
  }

  @Test
  public void testOnPrimaryCreate() {
    // A null tableId indicates a create.
    tableId = null;
    page.configure(type, fileName, parentId, fileHandleId, tableId);
    page.setModalPresenter(mockPresenter);
    // Seed suggestedSchema via callback before clicking Next.
    fireOnCsvPreviewDataChange(column);
    page.onPrimary();
    verify(mockCreateNextPage)
      .configure(this.fileName, this.parentId, this.uploadRequest, schema);
    verify(mockPresenter).setNextActivePage(mockCreateNextPage);
  }

  @Test
  public void testOnPrimaryAppend() {
    page.configure(type, fileName, parentId, fileHandleId, tableId);
    page.setModalPresenter(mockPresenter);
    // Seed suggestedSchema via callback before clicking Next.
    fireOnCsvPreviewDataChange(column);
    page.onPrimary();
    this.uploadRequest.setTableId(tableId);
    verify(mockAppendNextPage).configure(this.uploadRequest, schema);
    verify(mockPresenter).setNextActivePage(mockAppendNextPage);
  }
}
