package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.jsinterop.ColumnModelJsObject;
import org.sagebionetworks.web.client.jsinterop.UploadToTablePreviewResultJsObject;
import org.sagebionetworks.web.client.widget.CsvPreview;

public class UploadCSVPreviewPageImpl
  implements UploadCSVPreviewPage, UploadCSVPreviewPageView.Presenter {

  public static final String NEXT = "Next";
  // Injected dependencies.
  UploadCSVPreviewPageView view;
  CsvPreview csvPreview;
  CSVOptionsWidget csvOptionsWidget;
  UploadCSVFinishPage createNextPage;
  UploadCSVAppendPage appendNextPage;

  // dynamic data fields
  ContentTypeDelimiter type;
  String fileName;
  String parentId;
  String fileHandleId;
  String tableId;
  ModalPresenter presenter;
  List<ColumnModel> suggestedSchema;

  @Inject
  public UploadCSVPreviewPageImpl(
    UploadCSVPreviewPageView view,
    CSVOptionsWidget csvOptionsWidget,
    CsvPreview csvPreview,
    UploadCSVFinishPage createNextPage,
    UploadCSVAppendPage appendNextPage
  ) {
    this.view = view;
    this.csvPreview = csvPreview;
    this.csvOptionsWidget = csvOptionsWidget;
    this.createNextPage = createNextPage;
    this.appendNextPage = appendNextPage;
    view.setPresenter(this);
    this.view.setPreviewWidget(this.csvPreview);
    this.view.setCSVOptionsWidget(this.csvOptionsWidget);
  }

  @Override
  public Widget asWidget() {
    return this.view.asWidget();
  }

  @Override
  public void configure(
    ContentTypeDelimiter type,
    String fileName,
    String parentId,
    String fileHandleId,
    String tableId
  ) {
    this.type = type;
    this.fileName = fileName;
    this.parentId = parentId;
    this.fileHandleId = fileHandleId;
    this.tableId = tableId;
  }

  @Override
  public void onPrimary() {
    // Get the current options
    UploadToTablePreviewRequest currentOptions =
      csvOptionsWidget.getCurrentOptions();
    UploadToTableRequest uploadRequest = UploadRequestUtils.createFromPreview(
      currentOptions
    );
    if (this.tableId != null) {
      // This is an append.
      uploadRequest.setTableId(this.tableId);
      this.appendNextPage.configure(uploadRequest, suggestedSchema);
      this.presenter.setNextActivePage(this.appendNextPage);
      // For now just execute the next page. This may change in the future.
      this.appendNextPage.onPrimary();
    } else {
      // This is a create
      this.createNextPage.configure(
          fileName,
          parentId,
          uploadRequest,
          suggestedSchema
        );
      this.presenter.setNextActivePage(this.createNextPage);
    }
  }

  @Override
  public void setModalPresenter(final ModalPresenter presenter) {
    this.presenter = presenter;
    this.presenter.setInstructionMessage("");
    // Setup the CSV options using what we know about the file.
    this.csvOptionsWidget.configure(
        createDefaultPreviewRequest(),
        this::refreshPreview
      );
    refreshPreview();
  }

  /**
   * Build a default UploadToTablePreviewRequest using what we know about the file.
   *
   * @return
   */
  private UploadToTablePreviewRequest createDefaultPreviewRequest() {
    UploadToTablePreviewRequest previewRequest =
      new UploadToTablePreviewRequest();
    CsvTableDescriptor descriptor = new CsvTableDescriptor();
    descriptor.setSeparator(type.getDelimiter());
    previewRequest.setCsvTableDescriptor(descriptor);
    previewRequest.setUploadFileHandleId(fileHandleId);
    previewRequest.setDoFullFileScan(true);
    return previewRequest;
  }

  private void refreshPreview() {
    this.suggestedSchema = Collections.emptyList();
    this.presenter.setLoading(true);
    this.presenter.setPrimaryButtonText(NEXT);
    UploadToTablePreviewRequest previewRequest =
      csvOptionsWidget.getCurrentOptions();
    // React owns the fetch lifecycle. We pass current CSV options and receive
    // preview data/loading updates through callbacks.

    this.csvPreview.configure(
        fileHandleId,
        previewRequest.getCsvTableDescriptor(),
        // Called by React when preview data is ready
        data -> this.suggestedSchema = toSuggestedSchema(data),
        // Called by React when loading state changes
        isLoading -> presenter.setLoading(isLoading)
      );
  }

  /**
   * Maps the suggested columns from the React CsvPreview callback into
   * ColumnModel instances for use by the next wizard pages.
   */
  private List<ColumnModel> toSuggestedSchema(
    UploadToTablePreviewResultJsObject data
  ) {
    if (data == null || data.suggestedColumns == null) {
      return Collections.emptyList();
    }

    List<ColumnModel> result = new ArrayList<>(data.suggestedColumns.length);

    for (ColumnModelJsObject jsColumn : data.suggestedColumns) {
      if (jsColumn == null) {
        continue;
      }

      ColumnModel column = new ColumnModel();

      column.setId(jsColumn.id);
      column.setName(jsColumn.name);

      if (jsColumn.maximumSize != null) {
        column.setMaximumSize(jsColumn.maximumSize.longValue());
      }

      if (jsColumn.columnType != null) {
        try {
          column.setColumnType(ColumnType.valueOf(jsColumn.columnType));
        } catch (IllegalArgumentException e) {
          // Unknown column type returned by React
        }
      }
      result.add(column);
    }
    return result;
  }
}
