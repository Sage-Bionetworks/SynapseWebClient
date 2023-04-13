package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.download.AddBatchOfFilesToDownloadListResponse;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.EntityBadgeIconsProps;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.FileHandleUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;

public class EntityBadge
  implements SynapseWidgetPresenter, EntityBadgeView.Presenter {

  public static final String ADDED_TO_DOWNLOAD_LIST =
    " has been added to your download list.";
  public static final String LINK_SUCCESSFULLY_DELETED =
    "Successfully removed link";
  private final EntityBadgeView view;
  private final GlobalApplicationState globalAppState;
  private EntityHeader entityHeader;
  private final SynapseJavascriptClient jsClient;
  private final AuthenticationController authController;
  private final LazyLoadHelper lazyLoadHelper;
  private final PopupUtilsView popupUtils;
  private final EventBus eventBus;
  private ClickHandler customClickHandler;
  private final SynapseReactClientFullContextPropsProvider propsProvider;

  private final EntityBadgeIconsProps.OnUnlinkSuccess onUnlinkSuccess;
  private final EntityBadgeIconsProps.OnUnlinkError onUnlinkError;

  @Inject
  public EntityBadge(
    EntityBadgeView view,
    GlobalApplicationState globalAppState,
    SynapseJavascriptClient jsClient,
    LazyLoadHelper lazyLoadHelper,
    PopupUtilsView popupUtils,
    EventBus eventBus,
    AuthenticationController authController,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.view = view;
    this.globalAppState = globalAppState;
    this.jsClient = jsClient;
    this.lazyLoadHelper = lazyLoadHelper;
    this.popupUtils = popupUtils;
    this.eventBus = eventBus;
    this.authController = authController;
    this.propsProvider = propsProvider;

    Callback loadDataCallback = this::getEntityBundle;

    lazyLoadHelper.configure(loadDataCallback, view);
    view.setPresenter(this);

    onUnlinkSuccess =
      id -> {
        popupUtils.showInfo(LINK_SUCCESSFULLY_DELETED);
        globalAppState.refreshPage();
      };

    onUnlinkError = error -> popupUtils.showErrorMessage(error.getReason());
  }

  public void getEntityBundle() {
    EntityBundleRequest request = new EntityBundleRequest();
    request.setIncludeEntity(true);
    request.setIncludeFileHandles(true);

    jsClient.getEntityBundle(
      entityHeader.getId(),
      request,
      new AsyncCallback<EntityBundle>() {
        @Override
        public void onFailure(Throwable caught) {
          view.setError(caught.getMessage());
        }

        public void onSuccess(EntityBundle eb) {
          setEntityBundle(eb);
        }
      }
    );
  }

  public void configure(EntityHeader header) {
    entityHeader = header;
    configureViewWithEntityHeader();
    lazyLoadHelper.setIsConfigured();
  }

  public void configureViewWithEntityHeader() {
    view.clearEntityInformation();
    if (entityHeader != null) {
      view.setEntity(entityHeader);
      if (customClickHandler != null) {
        view.setClickHandler(customClickHandler);
      }
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setEntityBundle(EntityBundle eb) {
    view.clearIcons();

    if (customClickHandler != null) {
      view.setClickHandler(customClickHandler);
    }
    List<FileHandle> handles = eb.getFileHandles();
    FileHandle dataFileHandle = getDataFileHandle(handles);

    EntityBadgeIconsProps iconsProps = EntityBadgeIconsProps.create(
      eb.getEntity().getId(),
      onUnlinkSuccess,
      onUnlinkError
    );

    view.setIcons(iconsProps, propsProvider.getJsInteropContextProps());

    // In experimental mode, check if there's a bound JSON Schema + check validity

    view.setSize(getContentSize(dataFileHandle));
    view.setMd5(getContentMd5(dataFileHandle));

    if (
      eb.getEntity() instanceof FileEntity &&
      ((FileEntity) eb.getEntity()).getDataFileHandleId() != null
    ) {
      view.showAddToDownloadList();
    }
  }

  public static FileHandle getDataFileHandle(List<FileHandle> handles) {
    if (handles != null) {
      for (FileHandle handle : handles) {
        if (!FileHandleUtils.isPreviewFileHandle(handle)) {
          return handle;
        }
      }
    }
    return null;
  }

  public String getContentSize(FileHandle dataFileHandle) {
    if (dataFileHandle != null) {
      Long contentSize = dataFileHandle.getContentSize();
      if (contentSize != null && contentSize > 0) {
        return view.getFriendlySize(contentSize, true);
      }
    }
    return "";
  }

  public String getContentMd5(FileHandle dataFileHandle) {
    if (dataFileHandle != null) {
      return dataFileHandle.getContentMd5();
    }
    return "";
  }

  public EntityHeader getHeader() {
    return entityHeader;
  }

  public void setModifiedByUserBadgeClickHandler(ClickHandler handler) {
    view.setModifiedByUserBadgeClickHandler(handler);
  }

  public void setClickHandler(ClickHandler handler) {
    customClickHandler = handler;
  }

  public String getEntityId() {
    return entityHeader.getId();
  }

  @Override
  public void onAddToDownloadList() {
    if (!authController.isLoggedIn()) {
      globalAppState
        .getPlaceChanger()
        .goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
      return;
    }
    jsClient.addFileToDownloadListV2(
      entityHeader.getId(),
      entityHeader.getVersionNumber(),
      new AsyncCallback<AddBatchOfFilesToDownloadListResponse>() {
        @Override
        public void onFailure(Throwable caught) {
          view.setError(caught.getMessage());
        }

        public void onSuccess(AddBatchOfFilesToDownloadListResponse result) {
          String href = "#!DownloadCart:0";
          popupUtils.showInfo(
            entityHeader.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST,
            href,
            DisplayConstants.VIEW_DOWNLOAD_LIST
          );
          eventBus.fireEvent(new DownloadListUpdatedEvent());
        }
      }
    );
  }

  public void showMinimalColumnSet() {
    view.showMinimalColumnSet();
  }
}
