package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.aws.AwsSdk;
import org.sagebionetworks.web.client.widget.entity.download.Uploader;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.shared.WebConstants;

/**
 * Handles business logic for downloading files with various storage configurations.
 */
public class FileDownloadHandlerWidget
  implements FileDownloadMenuItemView.Presenter {

  public static final String ACCESS_REQUIREMENTS_LINK =
    "#!AccessRequirements:ID=";
  public static final String LOGIN_PLACE_LINK = "#!LoginPlace:0";
  private FileDownloadMenuItemView view;
  private EntityBundle entityBundle;
  private PortalGinInjector ginInjector;
  SynapseJavascriptClient jsClient;
  AuthenticationController authController;
  SynapseJSNIUtils jsniUtils;
  GWTWrapper gwt;
  CookieProvider cookies;
  AwsSdk awsSdk;
  PopupUtilsView popupUtilsView;
  FileHandle dataFileHandle;
  JavaScriptObject s3;

  @Inject
  public FileDownloadHandlerWidget(
    FileDownloadMenuItemView view,
    PortalGinInjector ginInjector,
    SynapseJavascriptClient jsClient,
    AuthenticationController authController,
    SynapseJSNIUtils jsniUtils,
    GWTWrapper gwt,
    CookieProvider cookies,
    AwsSdk awsSdk,
    PopupUtilsView popupUtilsView
  ) {
    this.view = view;
    this.ginInjector = ginInjector;
    this.jsClient = jsClient;
    this.authController = authController;
    this.jsniUtils = jsniUtils;
    this.gwt = gwt;
    this.cookies = cookies;
    this.awsSdk = awsSdk;
    this.popupUtilsView = popupUtilsView;
    view.setPresenter(this);
  }

  public void configure(
    final EntityActionMenu actionMenu,
    final EntityBundle bundle
  ) {
    view.clear();
    if (bundle.getRestrictionInformation() != null) {
      configure(actionMenu, bundle, bundle.getRestrictionInformation());
    } else {
      jsClient.getRestrictionInformation(
        bundle.getEntity().getId(),
        RestrictableObjectType.ENTITY,
        new AsyncCallback<RestrictionInformationResponse>() {
          @Override
          public void onFailure(Throwable caught) {
            handleException(caught);
          }

          public void onSuccess(
            RestrictionInformationResponse restrictionInformation
          ) {
            configure(actionMenu, bundle, restrictionInformation);
          }
        }
      );
    }
  }

  public void configure(
    final EntityActionMenu actionMenu,
    EntityBundle bundle,
    RestrictionInformationResponse restrictionInformation
  ) {
    view.clear();
    this.view.setActionMenu(actionMenu);
    this.entityBundle = bundle;
    dataFileHandle = getFileHandle();
    s3 = null;

    if (restrictionInformation.getHasUnmetAccessRequirement()) {
      // if in alpha, send to access requirements
      view.setIsDirectDownloadLink(
        ACCESS_REQUIREMENTS_LINK +
        bundle.getEntity().getId() +
        "&" +
        AccessRequirementsPlace.TYPE_PARAM +
        "=" +
        RestrictableObjectType.ENTITY.toString()
      );
    } else {
      if (dataFileHandle != null) {
        if (dataFileHandle instanceof ExternalObjectStoreFileHandle) {
          view.setIsUnauthenticatedS3DirectDownload();
        } else {
          String fileNameOverride = entityBundle.getFileName();
          String directDownloadUrl = getDirectDownloadURL(
            (FileEntity) entityBundle.getEntity(),
            dataFileHandle,
            fileNameOverride
          );

          // special case, if this starts with sftp proxy, then handle
          String sftpProxy = ginInjector
            .getSynapseProperties()
            .getSynapseProperty(WebConstants.SFTP_PROXY_ENDPOINT);
          if (directDownloadUrl.startsWith(sftpProxy)) {
            view.setIsSFTPDownload();
          } else {
            view.setIsDirectDownloadLink(directDownloadUrl);
          }
        }
      } else if (!authController.isLoggedIn()) {
        view.setIsDirectDownloadLink(LOGIN_PLACE_LINK);
      }
    }
  }

  private void handleException(Throwable t) {
    popupUtilsView.showErrorMessage(t.getMessage());
  }

  public FileHandle getFileHandle() {
    if (entityBundle != null && entityBundle.getEntity() != null) {
      if (entityBundle.getEntity() instanceof FileEntity) {
        return DisplayUtils.getFileHandle(entityBundle);
      }
    }
    return null;
  }

  public String getDirectDownloadURL(
    FileEntity fileEntity,
    FileHandle fileHandle,
    String fileNameOverride
  ) {
    String externalUrl = null;
    if (fileHandle instanceof ExternalFileHandle) {
      externalUrl = ((ExternalFileHandle) fileHandle).getExternalURL();
    }

    String directDownloadURL = null;
    if (
      externalUrl != null &&
      externalUrl.toLowerCase().startsWith(WebConstants.SFTP_PREFIX)
    ) {
      // point to sftp proxy instead
      directDownloadURL =
        Uploader.getSftpProxyLink(
          fileNameOverride,
          externalUrl,
          ginInjector.getSynapseProperties(),
          gwt
        );
    } else {
      directDownloadURL =
        jsniUtils.getFileHandleAssociationUrl(
          fileEntity.getId(),
          FileHandleAssociateType.FileEntity,
          fileHandle.getId()
        );
    }
    return directDownloadURL;
  }

  @Override
  public void onUnauthenticatedS3DirectDownloadClicked() {
    // ask for credentials, use bucket/endpoint info from storage location
    ExternalObjectStoreFileHandle objectStoreFileHandle = (ExternalObjectStoreFileHandle) dataFileHandle;
    view.showLoginS3DirectDownloadDialog(
      objectStoreFileHandle.getEndpointUrl()
    );
  }

  @Override
  public void onLoginS3DirectDownloadClicked(
    String accessKeyId,
    String secretAccessKey
  ) {
    final ExternalObjectStoreFileHandle objectStoreFileHandle = (ExternalObjectStoreFileHandle) dataFileHandle;
    CallbackP<JavaScriptObject> s3Callback = s3JsObject -> {
      s3 = s3JsObject;
      // NOTE: most browsers block the popup because the button click event is not directly associated to
      // the login popup.
      // Show the direct download button after authorization succeeds.
      view.showS3DirectDownloadDialog();
    };
    awsSdk.getS3(
      accessKeyId,
      secretAccessKey,
      objectStoreFileHandle.getBucket(),
      objectStoreFileHandle.getEndpointUrl(),
      s3Callback
    );
  }

  @Override
  public void onAuthenticatedS3DirectDownloadClicked() {
    ExternalObjectStoreFileHandle objectStoreFileHandle = (ExternalObjectStoreFileHandle) dataFileHandle;
    String presignedUrl = awsSdk.getPresignedURL(
      objectStoreFileHandle.getFileKey(),
      objectStoreFileHandle.getBucket(),
      objectStoreFileHandle.getFileName(),
      s3
    );
    popupUtilsView.openInNewWindow(presignedUrl);
  }

  @Override
  public void onSFTPDownloadErrorClicked() {
    popupUtilsView.showErrorMessage(
      DisplayConstants.ERROR_SFTP_DOWNLOAD_TITLE,
      DisplayConstants.ERROR_SFTP_DOWNLOAD_MESSAGE
    );
  }

  @Override
  public void onDirectDownloadClicked() {
    // user already directly sent to download fha, could use this hook to send an event to analytics if we want
  }
}
