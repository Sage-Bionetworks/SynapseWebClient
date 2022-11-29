package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.download.AddBatchOfFilesToDownloadListResponse;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class EntityListRowBadge
  implements
    EntityListRowBadgeView.Presenter,
    SynapseWidgetPresenter,
    SelectableListItem {

  public static final String N_A = "N/A";
  private EntityListRowBadgeView view;
  private UserBadge createdByUserBadge;
  private SynapseJavascriptClient jsClient;
  private String entityId, entityName;
  private Long version;
  private Callback selectionChangedCallback;
  private LazyLoadHelper lazyLoadHelper;
  private DateTimeUtils dateTimeUtils;
  FileHandle dataFileHandle;
  PopupUtilsView popupUtils;
  EventBus eventBus;
  SynapseJSNIUtils jsniUtils;
  CookieProvider cookies;

  @Inject
  public EntityListRowBadge(
    EntityListRowBadgeView view,
    UserBadge userBadge,
    SynapseJavascriptClient jsClient,
    LazyLoadHelper lazyLoadHelper,
    DateTimeUtils dateTimeUtils,
    PopupUtilsView popupUtils,
    EventBus eventBus,
    SynapseJSNIUtils jsniUtils,
    CookieProvider cookies
  ) {
    this.view = view;
    this.createdByUserBadge = userBadge;
    this.dateTimeUtils = dateTimeUtils;
    this.jsClient = jsClient;
    this.lazyLoadHelper = lazyLoadHelper;
    this.popupUtils = popupUtils;
    this.eventBus = eventBus;
    this.jsniUtils = jsniUtils;
    this.cookies = cookies;
    view.setCreatedByWidget(userBadge.asWidget());
    view.setPresenter(this);
    Callback loadDataCallback = new Callback() {
      @Override
      public void invoke() {
        getEntityBundle();
      }
    };

    lazyLoadHelper.configure(loadDataCallback, view);
  }

  public void setNote(String note) {
    view.setNote(note);
  }

  public void setDescriptionVisible(boolean visible) {
    view.setDescriptionVisible(visible);
  }

  public void setIsSelectable(boolean isSelectable) {
    view.setIsSelectable(isSelectable);
  }

  public boolean isSelected() {
    return view.isSelected();
  }

  public void setSelected(boolean isSelected) {
    view.setSelected(isSelected);
  }

  public void getEntityBundle() {
    EntityBundleRequest request = new EntityBundleRequest();
    request.setIncludeEntity(true);
    request.setIncludeFileHandles(true);
    view.showLoading();
    AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
      @Override
      public void onFailure(Throwable caught) {
        view.setEntityLink(
          entityId,
          DisplayUtils.getSynapseHistoryToken(entityId, version)
        );
        view.showErrorIcon(caught.getMessage());
      }

      public void onSuccess(EntityBundle eb) {
        setEntityBundle(eb);
      }
    };
    if (version == null) {
      jsClient.getEntityBundle(entityId, request, callback);
    } else {
      jsClient.getEntityBundleForVersion(entityId, version, request, callback);
    }
  }

  public void configure(Reference reference) {
    this.entityId = reference.getTargetId();
    this.version = reference.getTargetVersionNumber();

    lazyLoadHelper.setIsConfigured();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void setEntityBundle(EntityBundle eb) {
    view.setEntityType(EntityTypeUtils.getEntityType(eb.getEntity()));

    entityName = eb.getEntity().getName();
    view.setEntityLink(
      entityName,
      DisplayUtils.getSynapseHistoryToken(entityId, version)
    );
    if (eb.getEntity().getCreatedBy() != null) {
      createdByUserBadge.configure(eb.getEntity().getCreatedBy());
      createdByUserBadge.setOpenInNewWindow();
    }

    if (eb.getEntity().getCreatedOn() != null) {
      String dateString = dateTimeUtils.getDateTimeString(
        eb.getEntity().getCreatedOn()
      );
      view.setCreatedOn(dateString);
    } else {
      view.setCreatedOn("");
    }
    view.setDescription(eb.getEntity().getDescription());

    if (eb.getEntity() instanceof FileEntity) {
      dataFileHandle = EntityBadge.getDataFileHandle(eb.getFileHandles());
      view.showAddToDownloadList();
    }

    if (eb.getEntity() instanceof Versionable) {
      Versionable versionable = (Versionable) eb.getEntity();
      view.setVersion(versionable.getVersionNumber().toString());
      version = versionable.getVersionNumber();
    } else {
      view.setVersion(N_A);
    }
    view.showRow();
  }

  public EntityGroupRecord getRecord() {
    Reference ref = new Reference();
    ref.setTargetId(entityId);
    ref.setTargetVersionNumber(version);

    EntityGroupRecord record = new EntityGroupRecord();
    record.setEntityReference(ref);
    record.setNote(view.getNote());
    return record;
  }

  public String getNote() {
    return view.getNote();
  }

  public String getEntityId() {
    return entityId;
  }

  public void setSelectionChangedCallback(Callback selectionChangedCallback) {
    this.selectionChangedCallback = selectionChangedCallback;
  }

  @Override
  public void onSelectionChanged() {
    if (selectionChangedCallback != null) {
      selectionChangedCallback.invoke();
    }
  }

  @Override
  public void onAddToDownloadList() {
    jsClient.addFileToDownloadListV2(
      entityId,
      version,
      new AsyncCallback<AddBatchOfFilesToDownloadListResponse>() {
        @Override
        public void onFailure(Throwable caught) {
          view.showErrorIcon(caught.getMessage());
        }

        public void onSuccess(AddBatchOfFilesToDownloadListResponse result) {
          String href = "#!DownloadCart:0";
          popupUtils.showInfo(
            entityName + EntityBadge.ADDED_TO_DOWNLOAD_LIST,
            href,
            DisplayConstants.VIEW_DOWNLOAD_LIST
          );
          eventBus.fireEvent(new DownloadListUpdatedEvent());
        }
      }
    );
  }
}
