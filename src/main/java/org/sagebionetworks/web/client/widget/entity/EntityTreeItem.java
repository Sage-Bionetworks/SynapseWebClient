package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsTreeItem;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityTreeItem implements IsTreeItem, EntityTreeItemView.Presenter, SynapseWidgetPresenter {
	
	private EntityTreeItemView view;
	private EntityIconsCache iconsCache;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private ClientCache clientCache;
	private GlobalApplicationState globalAppState;
	private TreeItem treeItem;
	private EntityHeader entityHeader;
	
	@Inject
	public EntityTreeItem(EntityTreeItemView view, 
			EntityIconsCache iconsCache,
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalAppState,
			ClientCache clientCache) {
		this.view = view;
		this.iconsCache = iconsCache;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.clientCache = clientCache;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}
	
	public void configure(EntityHeader header) {
		view.setEntity(header);
		treeItem = new TreeItem(asWidget());		// TODO: Is this correct?
		entityHeader = header;
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
		getInfoEntity(entityId, synapseClient, adapterFactory, clientCache, callback);
	}
	
	// TODO: Add types to this method or something.
	private static void getInfoEntity(String entityId, 
			final SynapseClientAsync synapseClient,
			final AdapterFactory adapterFactory,
			final ClientCache clientCache,
			final AsyncCallback<KeyValueDisplay<String>> callback) {
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					//If necessary, expand to support other types.  
					//But do not pull in NodeAdapterFactory for the mapping, as this will cause the initial fragment download size to significantly increase.
//					if (!Project.class.getName().equals(result.getEntityClassName()) || !Data.class.getName().equals(result.getEntityClassName())) {
//						callback.onFailure(new IllegalArgumentException("Entity tree items detailed information currently only supports Projects"));
//					}
					
					// TODO: Haven't checked any of this. Also, hacky final thing.
					Entity preEntity = null;
					if (Project.class.getName().equals(result.getEntityClassName())) {
						preEntity = new Project(adapterFactory.createNew(result.getEntityJson()));
					} else if (Folder.class.getName().equals(result.getEntityClassName())) {
						preEntity = new Folder(adapterFactory.createNew(result.getEntityJson()));
					} else if (Data.class.getName().equals(result.getEntityClassName())) {
						preEntity = new Data(adapterFactory.createNew(result.getEntityJson()));
					} else if (FileEntity.class.getName().equals(result.getEntityClassName())) {
						preEntity = new FileEntity(adapterFactory.createNew(result.getEntityJson()));
					} else {
						callback.onFailure(new IllegalArgumentException("The class " + result.getEntityClassName() + " is not yet supported."));
					}
					final Entity entity = preEntity;
					
					//final Project entity = new Project(adapterFactory.createNew(result.getEntityJson()));
					UserBadge.getUserProfile(entity.getModifiedBy(), adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
						@Override
						public void onSuccess(UserProfile profile) {
							callback.onSuccess(ProvUtils.entityToKeyValueDisplay(entity, DisplayUtils.getDisplayName(profile)));		
						}
						@Override
						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}
					});
					
					
						
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
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

	@Override
	public TreeItem asTreeItem() {
		return treeItem;		// TODO: Should I make a new one every time?
	}
	
	@Override
	public void showLoadingChildren() {
		view.showLoadingChildren();
	}
	
	@Override
	public void showTypeIcon() {
		view.showTypeIcon();
	}
	
	@Override
	public EntityHeader getHeader() {
		return entityHeader;
	}

}
