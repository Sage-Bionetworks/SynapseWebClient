package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
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
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityBadgeView view;
	private GlobalApplicationState globalAppState;
	private EntityQueryResult entityHeader;
	private AnnotationTransformer transformer;
	private UserBadge modifiedByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	private CallbackP<String> customEntityClickHandler;
	private boolean hasData, hasRequestedData;
	@Inject
	public EntityBadge(EntityBadgeView view, 
			GlobalApplicationState globalAppState,
			AnnotationTransformer transformer,
			UserBadge modifiedByUserBadge,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.globalAppState = globalAppState;
		this.transformer = transformer;
		this.modifiedByUserBadge = modifiedByUserBadge;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
		view.setModifiedByWidget(modifiedByUserBadge.asWidget());
		hasData = false;
		hasRequestedData = false;
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
		Entity entity = eb.getEntity();
		Annotations annotations = eb.getAnnotations();
		String rootWikiId = eb.getRootWikiId();
		List<FileHandle> handles = eb.getFileHandles();
		view.setAnnotations(getAnnotationsHTML(annotations));
		view.setSize(getContentSize(handles));
		if(eb.getPermissions().getCanPublicRead()) {
			view.showPublicIcon();
		} else {
			view.showPrivateIcon();
		}
		boolean hasLocalSharingSettings = eb.getBenefactorAcl().getId().equals(entity.getId());
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
	
	/**
	 * Adds annotations and wiki status values to the given key value display
	 * @param keyValueDisplay
	 * @param annotations
	 */
	public String getAnnotationsHTML(Annotations annotations) {
		StringBuilder sb = new StringBuilder();
		List<Annotation> annotationList = transformer.annotationsToList(annotations);
		for (Annotation annotation : annotationList) {
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
	
	/**
	 * Returns true if widget is in view, does not already have data, and has not previously asked for data.
	 */
	public boolean isRequestingData() {
		if (!view.isInViewport() || hasData || hasRequestedData) {
			return false;
		}
		hasRequestedData = true;
		return true;
	}
}
