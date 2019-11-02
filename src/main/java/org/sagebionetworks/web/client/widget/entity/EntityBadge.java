package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.annotation.v2.Annotations;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.FileHandleUtils;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements SynapseWidgetPresenter, EntityBadgeView.Presenter {

	public static final String VIEW_DOWNLOAD_LIST = "view download list";
	public static final String ADDED_TO_DOWNLOAD_LIST = " has been added to your download list.";
	public static final String LINK_SUCCESSFULLY_DELETED = "Successfully removed link";
	private EntityBadgeView view;
	private GlobalApplicationState globalAppState;
	private EntityHeader entityHeader;
	private AnnotationTransformer transformer;
	private SynapseJavascriptClient jsClient;
	private AuthenticationController authController;
	private LazyLoadHelper lazyLoadHelper;
	private PopupUtilsView popupUtils;
	private SynapseProperties synapseProperties;
	private EventBus eventBus;
	private String dataFileHandleId;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public EntityBadge(EntityBadgeView view, GlobalApplicationState globalAppState, AnnotationTransformer transformer, SynapseJavascriptClient jsClient, LazyLoadHelper lazyLoadHelper, PopupUtilsView popupUtils, SynapseProperties synapseProperties, EventBus eventBus, AuthenticationController authController, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.transformer = transformer;

		this.jsClient = jsClient;
		this.lazyLoadHelper = lazyLoadHelper;
		this.popupUtils = popupUtils;
		this.synapseProperties = synapseProperties;
		this.eventBus = eventBus;
		this.authController = authController;
		this.jsniUtils = jsniUtils;
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				getEntityBundle();
			}
		};

		lazyLoadHelper.configure(loadDataCallback, view);
		view.setPresenter(this);
	}

	public void getEntityBundle() {
		jsClient.getEntityBundleFromCache(entityHeader.getId(), new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setError(caught.getMessage());
			}

			public void onSuccess(EntityBundle eb) {
				setEntityBundle(eb);
			};
		});
	}

	public void configure(EntityHeader header) {
		entityHeader = header;
		view.setEntity(header);
		view.setIcon(EntityTypeUtils.getIconTypeForEntityClassName(header.getType()));
		lazyLoadHelper.setIsConfigured();
	}

	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityBundle(EntityBundle eb) {
		Annotations annotations = eb.getAnnotations();
		String rootWikiId = eb.getRootWikiId();
		List<FileHandle> handles = eb.getFileHandles();
		FileHandle dataFileHandle = getDataFileHandle(handles);
		if (PublicPrivateBadge.isPublic(eb.getBenefactorAcl(), synapseProperties.getPublicPrincipalIds())) {
			view.showPublicIcon();
		} else {
			view.showPrivateIcon();
		}

		if (annotations != null) {
			Map<String, AnnotationsValue> annotationsMap = annotations.getAnnotations();
			if (!annotationsMap.isEmpty()) {
				view.setAnnotations(getAnnotationsHTML(annotationsMap));
			}
		}
		if (eb.getEntity() instanceof Link && eb.getPermissions().getCanDelete()) {
			view.showUnlinkIcon();
		}
		view.setSize(getContentSize(dataFileHandle));
		view.setMd5(getContentMd5(dataFileHandle));

		boolean hasLocalSharingSettings = eb.getBenefactorAcl().getId().equals(entityHeader.getId());
		if (hasLocalSharingSettings) {
			view.showSharingSetIcon();
		}

		if (DisplayUtils.isDefined(rootWikiId)) {
			view.showHasWikiIcon();
		}

		if (eb.getEntity() instanceof FileEntity && ((FileEntity) eb.getEntity()).getDataFileHandleId() != null) {
			dataFileHandleId = ((FileEntity) eb.getEntity()).getDataFileHandleId();
			view.showAddToDownloadList();
		}

		if (eb.getThreadCount() > 0) {
			view.showDiscussionThreadIcon();
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

	/**
	 * Adds annotations and wiki status values to the given key value display
	 * 
	 * @param keyValueDisplay
	 * @param annotations
	 */
	public String getAnnotationsHTML(Map<String, AnnotationsValue> annotations) {
		StringBuilder sb = new StringBuilder();
		for (String key : annotations.keySet()) {
			sb.append("<strong>");
			sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(key));
			sb.append("</strong>&nbsp;");
			AnnotationsValue value = annotations.get(key);
			sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(transformer.getFriendlyValues(value)));
			sb.append("<br />");
		}
		return sb.toString();
	}

	public EntityHeader getHeader() {
		return entityHeader;
	}

	public void setModifiedByUserBadgeClickHandler(ClickHandler handler) {
		view.setModifiedByUserBadgeClickHandler(handler);
	}

	public void setClickHandler(ClickHandler handler) {
		view.addClickHandler(handler);
	}

	public String getEntityId() {
		return entityHeader.getId();
	}

	@Override
	public void onUnlink() {
		// delete this Link
		jsClient.deleteEntityById(getEntityId(), new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(LINK_SUCCESSFULLY_DELETED);
				globalAppState.refreshPage();
			}
		});
	}

	@Override
	public void onAddToDownloadList() {
		if (!authController.isLoggedIn()) {
			globalAppState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
			return;
		}
		// TODO: add special popup to report how many items are in the current download list, and link to
		// download list.
		jsClient.addFileToDownloadList(dataFileHandleId, entityHeader.getId(), new AsyncCallback<DownloadList>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setError(caught.getMessage());
			}

			@Override
			public void onSuccess(DownloadList result) {
				jsniUtils.sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, "1");
				String href = "#!Profile:" + authController.getCurrentUserPrincipalId() + "/downloads";
				popupUtils.showInfo(entityHeader.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST, href, VIEW_DOWNLOAD_LIST, IconType.CHECK_CIRCLE);
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
		});
	}

	public void showMinimalColumnSet() {
		view.showMinimalColumnSet();
	}

	public void setEntityNameWidthPx(int width) {
		view.setEntityNameWidthPx(width);
	}
}
