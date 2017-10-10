package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;
import static org.sagebionetworks.repo.model.EntityBundle.THREAD_COUNT;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.ARE_YOU_SURE_YOU_WANT_TO_DELETE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.CONFIRM_DELETE_TITLE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETE_FOLDER_EXPLANATION;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.THE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WAS_SUCCESSFULLY_DELETED;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadButton;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements SynapseWidgetPresenter, EntityBadgeView.Presenter {
	private EntityBadgeView view;
	private GlobalApplicationState globalAppState;
	private EntityHeader entityHeader;
	private AnnotationTransformer transformer;
	private UserBadge modifiedByUserBadge;
	private SynapseJavascriptClient jsClient;
	private FileDownloadButton fileDownloadButton;
	private LazyLoadHelper lazyLoadHelper;
	private DateTimeUtils dateTimeUtils;
	private PopupUtilsView popupUtils;
	private Entity currentEntity;
	private String entityTypeDisplay;
	
	@Inject
	public EntityBadge(EntityBadgeView view, 
			GlobalApplicationState globalAppState,
			AnnotationTransformer transformer,
			UserBadge modifiedByUserBadge,
			SynapseJavascriptClient jsClient,
			FileDownloadButton fileDownloadButton,
			LazyLoadHelper lazyLoadHelper,
			DateTimeUtils dateTimeUtils,
			PopupUtilsView popupUtils) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.transformer = transformer;
		this.modifiedByUserBadge = modifiedByUserBadge;
		this.dateTimeUtils = dateTimeUtils;
		this.jsClient = jsClient;
		this.fileDownloadButton = fileDownloadButton;
		this.lazyLoadHelper = lazyLoadHelper;
		this.popupUtils = popupUtils;
		view.setModifiedByWidget(modifiedByUserBadge.asWidget());
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				getEntityBundle();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
		fileDownloadButton.setSize(ButtonSize.EXTRA_SMALL);
		fileDownloadButton.setEntityUpdatedHandler(new EntityUpdatedHandler() {
			@Override
			public void onPersistSuccess(EntityUpdatedEvent event) {
				getEntityBundle();
			}
		});
		view.setPresenter(this);
	}
	
	public void getEntityBundle() {
		int partsMask = ENTITY | ANNOTATIONS | ROOT_WIKI_ID | FILE_HANDLES | PERMISSIONS | BENEFACTOR_ACL | THREAD_COUNT;
		jsClient.getEntityBundle(entityHeader.getId(), partsMask, new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorIcon();
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
		view.setIcon(org.sagebionetworks.web.client.EntityTypeUtils.getIconTypeForEntityClassName(header.getType()));
		lazyLoadHelper.setIsConfigured();
	}
	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle eb) {
		currentEntity = eb.getEntity();
		entityTypeDisplay = EntityTypeUtils.getDisplayName(EntityTypeUtils.getEntityTypeForClass(currentEntity.getClass()));
		Annotations annotations = eb.getAnnotations();
		String rootWikiId = eb.getRootWikiId();
		List<FileHandle> handles = eb.getFileHandles();
		List<Annotation> annotationList = transformer.annotationsToList(annotations);
		if (!annotationList.isEmpty()) {
			view.showAnnotationsIcon();
			view.setAnnotations(getAnnotationsHTML(annotationList));
		}
		if (eb.getPermissions().getCanDelete()) {
			view.showDeleteIcon();
		}
		view.setSize(getContentSize(handles));
		view.setMd5(getContentMd5(handles));
		if (PublicPrivateBadge.isPublic(eb.getBenefactorAcl(), globalAppState.getPublicPrincipalIds())) {
			view.showPublicIcon();
		} else {
			view.showPrivateIcon();
		}
		boolean hasLocalSharingSettings = eb.getBenefactorAcl().getId().equals(entityHeader.getId());
		if (hasLocalSharingSettings) {
			view.showSharingSetIcon();
		}
		
		if (DisplayUtils.isDefined(rootWikiId)) {
			view.showHasWikiIcon();
		}
		
		if (eb.getEntity() instanceof FileEntity) {
			fileDownloadButton.configure(eb);
			fileDownloadButton.hideClientHelp();
			view.setFileDownloadButton(fileDownloadButton.asWidget());
		}
		
		view.setDiscussionThreadIconVisible(eb.getThreadCount() > 0);
		
		if (eb.getEntity().getModifiedBy() != null) {
			modifiedByUserBadge.configure(eb.getEntity().getModifiedBy());
			view.setModifiedByWidgetVisible(true);
		} else {
			view.setModifiedByWidgetVisible(false);
		}
		
		if (eb.getEntity().getModifiedOn() != null) {
			String modifiedOnString = dateTimeUtils.convertDateToSmallString(eb.getEntity().getModifiedOn());
			view.setModifiedOn(modifiedOnString);
		} else {
			view.setModifiedOn("");
		}
	}
	
	public String getContentSize(List<FileHandle> handles) {
		if (handles != null) {
			for (FileHandle handle: handles) {
				if (!(handle instanceof PreviewFileHandle)) {
					Long contentSize = handle.getContentSize();
					if (contentSize != null && contentSize > 0) {
						return view.getFriendlySize(contentSize, true);
					}
				}
			}
		}
		return "";
	}
	

	public String getContentMd5(List<FileHandle> handles) {
		if (handles != null) {
			for (FileHandle handle: handles) {
				if (!(handle instanceof PreviewFileHandle)) {
					return handle.getContentMd5();
				}
			}
		}
		return "";
	}
	
	/**
	 * Adds annotations and wiki status values to the given key value display
	 * @param keyValueDisplay
	 * @param annotations
	 */
	public String getAnnotationsHTML(List<Annotation> annotations) {
		StringBuilder sb = new StringBuilder();
		for (Annotation annotation : annotations) {
			String key = annotation.getKey();
			sb.append("<strong>");
			sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(key));
			sb.append("</strong>&nbsp;");
			sb.append(SafeHtmlUtils.htmlEscapeAllowEntities(transformer.getFriendlyValues(annotation)));
			sb.append("<br />");
		}
		return sb.toString();
	}
	
	public void hideLoadingIcon() {
		view.hideLoadingIcon();
	}

	public void showLoadingIcon() {
		view.showLoadingIcon();
	}
	
	public EntityHeader getHeader() {
		return entityHeader;
	}
	
	public void addClickHandler(ClickHandler handler) {
		modifiedByUserBadge.setCustomClickHandler(handler);
		view.addClickHandler(handler);
	}
	
	public void setEntityClickedHandler(final CallbackP<String> callback) {
		view.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callback.invoke(entityHeader.getId());
			}
		});
	}
	
	public String getEntityId() {
		return entityHeader.getId();
	}
	
	@Override
	public void onDelete() {
		String display = ARE_YOU_SURE_YOU_WANT_TO_DELETE+entityTypeDisplay+" "+currentEntity.getName()+"?";
		if (currentEntity instanceof Folder) {
			display += DELETE_FOLDER_EXPLANATION;
		}
		
		popupUtils.showConfirmDialog(CONFIRM_DELETE_TITLE,display, new Callback() {
			@Override
			public void invoke() {
				postConfirmedDeleteEntity();
			}
		});
		
	}
	
	public void postConfirmedDeleteEntity() {
		jsClient.deleteEntityById(getEntityId(), new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				popupUtils.showErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo(DELETED, THE + entityTypeDisplay + WAS_SUCCESSFULLY_DELETED); 
				globalAppState.refreshPage();
			}
		});
		
	}
}
