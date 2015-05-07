package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ROOT_WIKI_ID;

import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.annotation.AnnotationTransformer;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityBadgeView view;
	private EntityIconsCache iconsCache;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private ClientCache clientCache;
	private GlobalApplicationState globalAppState;
	private EntityHeader entityHeader;
	private AnnotationTransformer transformer;
	
	@Inject
	public EntityBadge(EntityBadgeView view, 
			EntityIconsCache iconsCache,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalAppState,
			ClientCache clientCache,
			AnnotationTransformer transformer) {
		this.view = view;
		this.iconsCache = iconsCache;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.clientCache = clientCache;
		this.globalAppState = globalAppState;
		this.transformer = transformer;
		view.setPresenter(this);
	}
	
	public void configure(EntityHeader header) {
		entityHeader = header;
		view.setEntity(header);
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
		getInfoEntity(entityId, synapseClient, adapterFactory, clientCache, transformer, callback);
	}
	
	private static void getInfoEntity(String entityId, 
			final SynapseClientAsync synapseClient,
			final AdapterFactory adapterFactory,
			final ClientCache clientCache,
			final AnnotationTransformer transformer,
			final AsyncCallback<KeyValueDisplay<String>> callback) {
		int mask = ENTITY | ANNOTATIONS | ROOT_WIKI_ID;
		synapseClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle result) {
				final EntityBundle entity = result;
				if (entity == null) {
					callback.onFailure(new IllegalArgumentException("Null is not supported for entity badge detailed information."));
					return;
				}
				UserBadge.getUserProfile(entity.getEntity().getModifiedBy(), adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
					@Override
					public void onSuccess(UserProfile profile) {
						KeyValueDisplay<String> keyValueDisplay = ProvUtils.entityToKeyValueDisplay(entity.getEntity(), DisplayUtils.getDisplayName(profile), false);
						ProvUtils.addAnnotationsAndWikiStatus(transformer, keyValueDisplay, entity.getAnnotations(), entity.getRootWikiId());
						callback.onSuccess(keyValueDisplay);		
					}
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void entityClicked(EntityHeader entityHeader) {
		globalAppState.getPlaceChanger().goTo(new Synapse(entityHeader.getId()));
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
	
	public void setClickHandler(ClickHandler handler) {
		view.setClickHandler(handler);
	}

}
