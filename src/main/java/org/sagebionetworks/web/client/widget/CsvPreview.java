package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.web.client.jsinterop.CsvPreviewProps;

public interface CsvPreview extends IsWidget {
  void configure(
    String fileHandleId,
    CsvTableDescriptor csvTableDescriptor,
    CsvPreviewProps.OnCsvPreviewDataChangeFunction onCsvPreviewDataChange,
    CsvPreviewProps.OnIsLoadingChangeFunction onIsLoadingChange
  );
}
