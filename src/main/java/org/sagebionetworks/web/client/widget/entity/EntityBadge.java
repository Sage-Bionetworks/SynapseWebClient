package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.FILE_HANDLES;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.PreviewFileHandle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	public static final int DELAY_UNTIL_IN_VIEW = 2000;
	private EntityBadgeView view;
	private GlobalApplicationState globalAppState;
	private EntityQueryResult entityHeader;
	private AnnotationTransformer transformer;
	private UserBadge modifiedByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	private SynapseClientAsync synapseClient;
	private GWTWrapper gwt;
	private CallbackP<String> customEntityClickHandler;
	private Callback invokeCheckForInViewAndLoadData;
	private boolean isConfigured;
	private boolean isAttached;
	@Inject
	public EntityBadge(EntityBadgeView view, 
			GlobalApplicationState globalAppState,
			AnnotationTransformer transformer,
			UserBadge modifiedByUserBadge,
			SynapseJSNIUtils synapseJSNIUtils,
			SynapseClientAsync synapseClient,
			GWTWrapper gwt) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.transformer = transformer;
		this.modifiedByUserBadge = modifiedByUserBadge;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		view.setPresenter(this);
		view.setModifiedByWidget(modifiedByUserBadge.asWidget());
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};
		isConfigured = false;
		isAttached = false;
	}
	public void startCheckingIfAttachedAndConfigured() {
		if (isAttached && isConfigured) {
			checkForInViewAndLoadData();
		}
	}
	public void checkForInViewAndLoadData() {
		if (!view.isAttached()) {
			//Done, view has been detached and widget was never in the viewport
			return;
		} else if (view.isInViewport()) {
			//try to load data!
			getEntityBundle();
		} else {
			//wait for a few seconds and see if we should load data
			gwt.scheduleExecution(invokeCheckForInViewAndLoadData, DELAY_UNTIL_IN_VIEW);
		}
	}
	@Override
	public void viewAttached() {
		isAttached = true;
		startCheckingIfAttachedAndConfigured();
	}
	
	public void getEntityBundle() {
		int partsMask = ANNOTATIONS | ROOT_WIKI_ID | FILE_HANDLES | PERMISSIONS | BENEFACTOR_ACL;
		synapseClient.getEntityBundle(entityHeader.getId(), partsMask, new AsyncCallback<EntityBundle>() {
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
	
	public void configure(EntityQueryResult header) {
		entityHeader = header;
		view.setEntity(header);
		view.setIcon(getIconTypeForEntityType(header.getEntityType()));
		if (header.getModifiedByPrincipalId() != null) {
			modifiedByUserBadge.configure(header.getModifiedByPrincipalId().toString());
			view.setModifiedByWidgetVisible(true);
		} else {
			view.setModifiedByWidgetVisible(false);
		}
		
		if (header.getModifiedOn() != null) {
			String modifiedOnString = synapseJSNIUtils.convertDateToSmallString(header.getModifiedOn());
			view.setModifiedOn(modifiedOnString);
		} else {
			view.setModifiedOn("");
		}
		isConfigured = true;
		startCheckingIfAttachedAndConfigured();
	}
	

	public static IconType getIconTypeForEntityType(String entityType) {
		String className = FileEntity.class.getName();
		if (entityType != null) {
			if (entityType.equalsIgnoreCase("file")) {
				className = FileEntity.class.getName();
			} else if (entityType.equalsIgnoreCase("folder")) {
				className = Folder.class.getName();
			} else if (entityType.equalsIgnoreCase("project")) {
				className = Project.class.getName();
			} else if (entityType.equalsIgnoreCase("table")) {
				className = TableEntity.class.getName();
			} else if (entityType.equalsIgnoreCase("link")) {
				className = Link.class.getName();
			}
		}
		return DisplayUtils.getIconTypeForEntityClassName(className);
	}
	
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityBundle(EntityBundle eb) {
		Annotations annotations = eb.getAnnotations();
		String rootWikiId = eb.getRootWikiId();
		List<FileHandle> handles = eb.getFileHandles();
		List<Annotation> annotationList = transformer.annotationsToList(annotations);
		if (!annotationList.isEmpty()) {
			view.showAnnotationsIcon();
			view.setAnnotations(getAnnotationsHTML(annotationList));
		}
		
		view.setSize(getContentSize(handles));
		view.setMd5(getContentMd5(handles));
		if(eb.getPermissions().getCanPublicRead()) {
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
	
	
	public void setEntityClickedHandler(CallbackP<String> callback) {
		customEntityClickHandler = callback;
	}
	
	@Override
	public void entityClicked(EntityQueryResult entityHeader) {
		showLoadingIcon();
		if (customEntityClickHandler == null) {
			globalAppState.getPlaceChanger().goTo(new Synapse(entityHeader.getId()));	
		} else {
			customEntityClickHandler.invoke(entityHeader.getId());
		}
	}
	
	public void hideLoadingIcon() {
		view.hideLoadingIcon();
	}

	public void showLoadingIcon() {
		view.showLoadingIcon();
	}
	
	public EntityQueryResult getHeader() {
		return entityHeader;
	}
	
	public void setClickHandler(ClickHandler handler) {
		modifiedByUserBadge.setCustomClickHandler(handler);
		view.setClickHandler(handler);
	}
	
	public String getEntityId() {
		return entityHeader.getId();
	}
}
