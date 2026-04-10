package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.web.client.jsinterop.CsvPreviewProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class CsvPreviewImpl extends ReactComponent implements CsvPreview {

  @Inject
  public CsvPreviewImpl() {}

  @Override
  public void configure(
    String fileHandleId,
    CsvTableDescriptor csvTableDescriptor,
    CsvPreviewProps.OnCsvPreviewDataChangeFunction onCsvPreviewDataChange,
    CsvPreviewProps.OnIsLoadingChangeFunction onIsLoadingChange
  ) {
    CsvPreviewProps props = CsvPreviewProps.create(
      fileHandleId,
      csvTableDescriptor,
      onCsvPreviewDataChange,
      onIsLoadingChange
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CsvPreview,
      props
    );
    this.render(component);
  }
}
