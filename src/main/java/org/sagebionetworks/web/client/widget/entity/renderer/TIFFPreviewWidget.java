package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ClientProperties.MB;
import static org.sagebionetworks.web.client.ClientProperties.UTIF_JS;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.resources.ResourceLoader;

public class TIFFPreviewWidget implements IsWidget {

  private ResourceLoader resourceLoader;
  private SynapseJSNIUtils jsniUtils;
  private GWTWrapper gwt;
  FileHandleAssociation fha;
  TIFFPreviewWidgetView view;
  public static final double MAX_TIFF_FILE_SIZE = 10 * MB;
  public static String friendlyMaxFileSize = null;
  FileHandle fileHandle;
  AsyncCallback<Void> callback;

  @Inject
  public TIFFPreviewWidget(
    TIFFPreviewWidgetView view,
    ResourceLoader resourceLoader,
    SynapseJSNIUtils jsniUtils,
    GWTWrapper gwt
  ) {
    this.view = view;
    this.jsniUtils = jsniUtils;
    this.resourceLoader = resourceLoader;
    this.gwt = gwt;
    if (friendlyMaxFileSize == null) {
      friendlyMaxFileSize = gwt.getFriendlySize(MAX_TIFF_FILE_SIZE, true);
    }
    ClientProperties.fixResourceToCdnEndpoint(
      UTIF_JS,
      jsniUtils.getCdnEndpoint()
    );

    callback =
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          refreshContent();
        }

        @Override
        public void onFailure(Throwable caught) {}
      };
  }

  public void configure(String synapseId, FileHandle fileHandle) {
    this.fileHandle = fileHandle;
    fha = new FileHandleAssociation();
    fha.setAssociateObjectId(synapseId);
    fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
    fha.setFileHandleId(fileHandle.getId());
    if (!resourceLoader.isLoaded(UTIF_JS)) {
      resourceLoader.requires(UTIF_JS, callback);
    } else {
      callback.onSuccess(null);
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void refreshContent() {
    if (
      fileHandle.getContentSize() != null &&
      fileHandle.getContentSize() < MAX_TIFF_FILE_SIZE
    ) {
      if (fha != null) {
        String url = jsniUtils.getFileHandleAssociationUrl(
          fha.getAssociateObjectId(),
          fha.getAssociateObjectType(),
          fha.getFileHandleId()
        );
        view.configure(url);
      }
    } else {
      view.showError(
        "The TIFF preview was not shown because the file size (" +
        gwt.getFriendlySize(fileHandle.getContentSize().doubleValue(), true) +
        ") exceeds the maximum preview size (" +
        friendlyMaxFileSize +
        ")"
      );
    }
  }
}
