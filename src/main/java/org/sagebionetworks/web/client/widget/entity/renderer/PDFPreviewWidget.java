package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ClientProperties.MB;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

public class PDFPreviewWidget implements IsWidget {

  private IFrameView view;
  private SynapseJSNIUtils jsniUtils;
  private GWTWrapper gwt;
  FileHandleAssociation fha;

  public static final double MAX_PDF_FILE_SIZE = 30 * MB;
  public static final int DEFAULT_HEIGHT_PX = 800;
  public static String friendlyMaxPdfFileSize = null;
  FileHandle fileHandle;

  @Inject
  public PDFPreviewWidget(
    IFrameView view,
    SynapseJSNIUtils jsniUtils,
    GWTWrapper gwt
  ) {
    this.view = view;
    this.jsniUtils = jsniUtils;
    this.gwt = gwt;
    view.addAttachHandler(event -> {
      if (event.isAttached()) {
        refreshContent();
      }
    });
    if (friendlyMaxPdfFileSize == null) {
      friendlyMaxPdfFileSize = gwt.getFriendlySize(MAX_PDF_FILE_SIZE, true);
    }
  }

  public void configure(String synapseId, FileHandle fileHandle) {
    this.fileHandle = fileHandle;
    fha = new FileHandleAssociation();
    fha.setAssociateObjectId(synapseId);
    fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
    fha.setFileHandleId(fileHandle.getId());
    refreshContent();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void refreshContent() {
    if (
      fileHandle.getContentSize() != null &&
      fileHandle.getContentSize() < MAX_PDF_FILE_SIZE
    ) {
      if (fha != null) {
        String url = jsniUtils.getFileHandleAssociationUrl(
          fha.getAssociateObjectId(),
          fha.getAssociateObjectType(),
          fha.getFileHandleId()
        );
        final int height = view.getParentOffsetHeight() > 0
          ? view.getParentOffsetHeight()
          : DEFAULT_HEIGHT_PX;
        fetchAsBlobUrl(
          url,
          new AsyncCallback<String>() {
            @Override
            public void onSuccess(String blobUrl) {
              view.configure(blobUrl, height);
            }

            @Override
            public void onFailure(Throwable caught) {
              view.showError(
                "Failed to load PDF preview: " + caught.getMessage()
              );
            }
          }
        );
      }
    } else {
      view.showError(
        "The PDF preview was not shown because the file size (" +
        gwt.getFriendlySize(fileHandle.getContentSize().doubleValue(), true) +
        ") exceeds the maximum preview size (" +
        friendlyMaxPdfFileSize +
        ")"
      );
    }
  }

  private static native void fetchAsBlobUrl(
    String url,
    AsyncCallback<String> callback
  ) /*-{
		fetch(url, {credentials: 'same-origin'})
			.then(function(r) { return r.blob(); })
			.then(function(blob) {
				var objectUrl = URL.createObjectURL(blob);
				callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(*)(objectUrl);
			})
			['catch'](function(e) {
				var ex = @java.lang.RuntimeException::new(Ljava/lang/String;)(e.message || String(e));
				callback.@com.google.gwt.user.client.rpc.AsyncCallback::onFailure(*)(ex);
			});
	}-*/;
}
