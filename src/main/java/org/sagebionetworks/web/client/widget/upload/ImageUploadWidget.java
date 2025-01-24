package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental2.dom.Blob;
import elemental2.dom.FileList;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJsInteropUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class ImageUploadWidget implements ImageUploadView.Presenter, IsWidget {

  private ImageUploadView view;
  private MultipartUploader multipartUploader;
  private SynapseAlert synAlert;
  private CallbackP<FileUpload> finishedUploadingCallback;
  private Callback startedUploadingCallback;
  private SynapseJsInteropUtils synapseJsInteropUtils;
  private FileMetadata fileMeta;
  private ImageFileValidator validator = new ImageFileValidator();
  private PortalGinInjector ginInjector;

  @Inject
  public ImageUploadWidget(
    MultipartUploader multipartUploader,
    SynapseJsInteropUtils synapseJsInteropUtils,
    SynapseAlert synAlert,
    PortalGinInjector ginInjector
  ) {
    super();
    this.synAlert = synAlert;
    this.multipartUploader = multipartUploader;
    this.synapseJsInteropUtils = synapseJsInteropUtils;
    this.ginInjector = ginInjector;
  }

  public ImageUploadView getView() {
    if (view == null) {
      view = ginInjector.getImageUploadView();
      setView(view);
    }
    return view;
  }

  public void setView(ImageUploadView view) {
    this.view = view;
    this.view.setPresenter(this);
    view.setSynAlert(synAlert);
  }

  @Override
  public Widget asWidget() {
    return getView().asWidget();
  }

  public void addStyleName(String styleName) {
    getView().asWidget().addStyleName(styleName);
  }

  public void setVisible(boolean visible) {
    getView().asWidget().setVisible(visible);
  }

  public void configure(CallbackP<FileUpload> finishedUploadingCallback) {
    this.finishedUploadingCallback = finishedUploadingCallback;
    reset();
  }

  public void setUploadingCallback(Callback startedUploadingCallback) {
    this.startedUploadingCallback = startedUploadingCallback;
  }

  public void setUploadedFileText(String text) {
    getView().setUploadedFileText(text);
  }

  public FileMetadata getSelectedFileMetadata() {
    String inputId = getView().getInputId();
    FileList fileList = synapseJsInteropUtils.getFileList(inputId);
    String[] fileNames = synapseJsInteropUtils.getMultipleUploadFileNames(
      fileList
    );
    if (fileNames != null && fileNames.length > 0) {
      String name = fileNames[0];
      double fileSize = fileList.item(0).size;
      String contentType = fileList.item(0).type;
      return new FileMetadata(name, contentType, fileSize);
    }
    return null;
  }

  @Override
  public void onFileSelected() {
    synAlert.clear();
    FileMetadata fileMeta = getSelectedFileMetadata();
    if (fileMeta != null) {
      if (validator.isValid(fileMeta)) {
        getView().processFile();
      } else {
        Callback invalidFileCallback = validator.getInvalidFileCallback();
        if (invalidFileCallback == null) {
          String invalidMessage = validator.getInvalidMessage();
          if (invalidMessage == null) {
            synAlert.showError("Please select a valid filetype.");
          } else {
            synAlert.showError(invalidMessage);
          }
        } else {
          invalidFileCallback.invoke();
        }
      }
    }
  }

  @Override
  public void onFileProcessed(Blob blob, String forcedContentType) {
    synAlert.clear();
    fileMeta = getSelectedFileMetadata();
    if (fileMeta != null && blob != null) {
      if (startedUploadingCallback != null) {
        startedUploadingCallback.invoke();
      }
      getView().updateProgress(1, "1%");
      getView().showProgress(true);
      getView().setInputEnabled(false);
      if (forcedContentType != null) {
        fileMeta.setContentType(forcedContentType);
      }
      doMultipartUpload(fileMeta, blob);
    }
  }

  public void reset() {
    getView().setInputEnabled(true);
    getView().showProgress(false);
    synAlert.clear();
    getView().resetForm();
  }

  public void setButtonType(ButtonType type) {
    getView().setButtonType(type);
  }

  public void setButtonSize(ButtonSize size) {
    getView().setButtonSize(size);
  }

  public void setButtonText(String text) {
    getView().setButtonText(text);
  }

  public void setButtonIcon(IconType iconType) {
    getView().setButtonIcon(iconType);
  }

  private void doMultipartUpload(final FileMetadata fileMeta, Blob blob) {
    // The uploader does the real work
    multipartUploader.uploadFile(
      fileMeta.getFileName(),
      fileMeta.getContentType(),
      blob,
      new ProgressingFileUploadHandler() {
        @Override
        public void uploadSuccess(String fileHandleId) {
          FileUpload uploadedFile = new FileUpload(fileMeta, fileHandleId);
          getView().updateProgress(100, "100%");
          getView().showProgress(false);
          getView().setInputEnabled(true);
          finishedUploadingCallback.invoke(uploadedFile);
        }

        @Override
        public void uploadFailed(String error) {
          getView().showProgress(false);
          getView().setInputEnabled(true);
          synAlert.showError(error);
        }

        @Override
        public void updateProgress(
          double currentProgress,
          String progressText,
          String uploadSpeed
        ) {
          int totalProgress = (int) (currentProgress * 100);
          getView().updateProgress(totalProgress, totalProgress + "%");
        }
      },
      null,
      getView()
    );
  }
}
