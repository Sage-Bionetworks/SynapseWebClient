package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
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
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityBadgeView view;
	private EntityIconsCache iconsCache;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private EntityQueryResult entityHeader;
	private AnnotationTransformer transformer;
	private UserBadge modifiedByUserBadge;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public EntityBadge(EntityBadgeView view, 
			EntityIconsCache iconsCache,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalAppState,
			AnnotationTransformer transformer,
			UserBadge modifiedByUserBadge,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.iconsCache = iconsCache;
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
		
		if (header.getModifiedByPrincipalId() != null) {
			modifiedByUserBadge.configure(header.getModifiedByPrincipalId().toString());
			modifiedByUserBadge.asWidget().setVisible(true);
		} else {
			modifiedByUserBadge.asWidget().setVisible(false);
		}
		
		if (header.getModifiedOn() != null) {
			String modifiedOnString = synapseJSNIUtils.convertDateToSmallString(header.getModifiedOn());
			view.setModifiedOn(modifiedOnString);
		} else {
			view.setModifiedOn("");
		}
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public ImageResource getIconForType(String type) {
		return iconsCache.getIconForType(type);
	}
	
	@Override
	public void getInfo(String entityId, final AsyncCallback<KeyValueDisplay<String>> callback) {
		synapseClient.getEntityInfo(entityId, new AsyncCallback<EntityBundlePlus>() {
			@Override
			public void onSuccess(EntityBundlePlus result) {
				Entity entity = result.getEntityBundle().getEntity();
				Annotations annotations = result.getEntityBundle().getAnnotations();
				String rootWikiId = result.getEntityBundle().getRootWikiId();
				KeyValueDisplay<String> keyValueDisplay = ProvUtils.entityToKeyValueDisplay(entity, DisplayUtils.getDisplayName(result.getProfile()), false);
				addAnnotationsAndWikiStatus(keyValueDisplay, annotations, rootWikiId);
				callback.onSuccess(keyValueDisplay);		
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
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
		view.setClickHandler(handler);
	}

}
