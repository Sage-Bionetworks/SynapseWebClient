package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.jsinterop.AddToDownloadListConfirmationAlertProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class AddToDownloadListV2Impl implements AddToDownloadListV2 {

  ReactComponent container = new ReactComponent();

  String queryJson;
  String folderId;
  JSONObjectAdapter adapter;

  @Inject
  public AddToDownloadListV2Impl(JSONObjectAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public void configure(String entityId, Query query) {
    this.folderId = null;
    try {
      JSONObjectAdapter newAdapter = adapter.createNew();
      query.writeToJSONObject(newAdapter);
      this.queryJson = newAdapter.toJSONString();
    } catch (JSONObjectAdapterException e) {
      e.printStackTrace();
    }
    init();
  }

  @Override
  public void configure(String folderId) {
    this.queryJson = null;
    this.folderId = folderId;
    init();
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  private void init() {
    container.setVisible(true);
    AddToDownloadListConfirmationAlertProps.Callback onClose = () ->
      container.setVisible(false);
    AddToDownloadListConfirmationAlertProps props;
    if (folderId != null) {
      props =
        AddToDownloadListConfirmationAlertProps.createForContainer(
          folderId,
          onClose
        );
    } else {
      props =
        AddToDownloadListConfirmationAlertProps.createForQuery(
          queryJson,
          onClose
        );
    }
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.AddToDownloadListConfirmationAlert,
      props
    );
    container.render(component);
  }
}
