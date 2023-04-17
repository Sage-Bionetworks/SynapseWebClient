package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.DownloadConfirmationProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class AddToDownloadListV2Impl implements AddToDownloadListV2 {

  private SynapseReactClientFullContextPropsProvider propsProvider;

  ReactComponentDiv container = new ReactComponentDiv();

  String queryBundleRequestJson;
  String folderId;
  JSONObjectAdapter adapter;

  @Inject
  public AddToDownloadListV2Impl(
    SynapseReactClientFullContextPropsProvider propsProvider,
    JSONObjectAdapter adapter
  ) {
    this.propsProvider = propsProvider;
    this.adapter = adapter;
  }

  @Override
  public void configure(String entityId, Query query) {
    try {
      QueryBundleRequest queryBundleRequest = new QueryBundleRequest();
      queryBundleRequest.setEntityId(entityId);
      queryBundleRequest.setQuery(query);
      JSONObjectAdapter newAdapter = adapter.createNew();
      queryBundleRequest.writeToJSONObject(newAdapter);
      this.queryBundleRequestJson = newAdapter.toJSONString();
    } catch (JSONObjectAdapterException e) {
      e.printStackTrace();
    }
    init();
  }

  @Override
  public void configure(String folderId) {
    this.folderId = folderId;
    init();
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  private void init() {
    container.setVisible(true);
    DownloadConfirmationProps.Callback onClose = new DownloadConfirmationProps.Callback() {
      @Override
      public void run() {
        container.setVisible(false);
      }
    };
    DownloadConfirmationProps editorProps = DownloadConfirmationProps.create(
      queryBundleRequestJson,
      folderId,
      onClose
    );
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DownloadConfirmation,
      editorProps,
      propsProvider.getJsInteropContextProps()
    );
    container.render(component);
  }
}
