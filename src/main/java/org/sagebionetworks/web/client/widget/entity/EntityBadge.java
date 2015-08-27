package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.EntityBundlePlus;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityBadgeView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private EntityQueryResult entityHeader;
	private AnnotationTransformer transformer;
	private UserBadge modifiedByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public EntityBadge(EntityBadgeView view, 
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AnnotationTransformer transformer,
			UserBadge modifiedByUserBadge,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalAppState = globalAppState;
		this.transformer = transformer;
		this.modifiedByUserBadge = modifiedByUserBadge;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
		view.setModifiedByWidget(modifiedByUserBadge.asWidget());
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
	
	@Override
	public void getInfo(String entityId, final AsyncCallback<KeyValueDisplay<String>> callback) {
		synapseClient.getEntityInfo(entityId, new AsyncCallback<EntityBundlePlus>() {
			@Override
			public void onSuccess(EntityBundlePlus result) {
				EntityBundle eb = result.getEntityBundle();
				Entity entity = eb.getEntity();
				Annotations annotations = eb.getAnnotations();
				String rootWikiId = eb.getRootWikiId();
				List<FileHandle> handles = eb.getFileHandles();
				KeyValueDisplay<String> keyValueDisplay = ProvUtils.entityToKeyValueDisplay(entity, DisplayUtils.getDisplayName(result.getProfile()), false);
				addAnnotationsAndWikiStatus(keyValueDisplay, annotations, rootWikiId);
				addContentSize(keyValueDisplay, handles);
				callback.onSuccess(keyValueDisplay);		
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	public void addContentSize(KeyValueDisplay<String> keyValueDisplay, List<FileHandle> handles) {
		for (FileHandle handle: handles) {
		}
	}
	
	/**
	 * Adds annotations and wiki status values to the given key value display
	 * @param keyValueDisplay
	 * @param annotations
	 * @param rootWikiKeyId
	 */
	public void addAnnotationsAndWikiStatus(KeyValueDisplay<String> keyValueDisplay, Annotations annotations, String rootWikiKeyId) {
		Map<String,String> map = keyValueDisplay.getMap();
		List<String> order = keyValueDisplay.getKeyDisplayOrder();
		
		List<Annotation> annotationList = transformer.annotationsToList(annotations);
		for (Annotation annotation : annotationList) {
			String key = annotation.getKey();
			order.add(key);
			map.put(key, SafeHtmlUtils.htmlEscapeAllowEntities(transformer.getFriendlyValues(annotation)));
		}
		if (DisplayUtils.isDefined(rootWikiKeyId)) {
			order.add("*Note");
			map.put("*Note", "Has a wiki");
		}
	}
	
	@Override
	public void entityClicked(EntityQueryResult entityHeader) {
		globalAppState.getPlaceChanger().goTo(new Synapse(entityHeader.getId()));
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

}
