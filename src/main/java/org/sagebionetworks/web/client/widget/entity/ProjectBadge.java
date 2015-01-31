package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBadge implements ProjectBadgeView.Presenter, SynapseWidgetPresenter {
	
	private ProjectBadgeView view;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private GlobalApplicationState globalAppState;
	private ClientCache clientCache;
	private ProjectHeader header;
	
	@Inject
	public ProjectBadge(ProjectBadgeView view, 
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalAppState,
			ClientCache clientCache
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.globalAppState = globalAppState;
		this.clientCache = clientCache;
		view.setPresenter(this);
	}
	
	public void configure(ProjectHeader header) {
		this.header = header;
		view.setLastActivityVisible(false);
		if (header != null) {
			if (header.getLastActivity() != null) {
				try {
					String dateString = view.getSimpleDateString(header.getLastActivity());
					view.setLastActivityVisible(true);
					view.setLastActivityText(dateString);
				} catch(Exception e) {};
			}
			view.setProject(header.getName(), header.getId());
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	
	@Override
	public void getInfo(AsyncCallback<KeyValueDisplay<String>> callback) {
		getInfoEntity(header.getId(), synapseClient, adapterFactory, clientCache, callback);
	}
	
	private static void getInfoEntity(String entityId, 
			final SynapseClientAsync synapseClient,
			final AdapterFactory adapterFactory,
			final ClientCache clientCache,
			final AsyncCallback<KeyValueDisplay<String>> callback) {
		synapseClient.getEntity(entityId, new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				try {
					final Project entity = new Project(adapterFactory.createNew(result.getEntityJson()));
					UserBadge.getUserProfile(entity.getModifiedBy(), adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
						@Override
						public void onSuccess(UserProfile profile) {
							callback.onSuccess(ProvUtils.entityToKeyValueDisplay(entity, DisplayUtils.getDisplayName(profile), false));		
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
	public void entityClicked() {
		globalAppState.getPlaceChanger().goTo(new Synapse(header.getId()));
	}
	
	public ProjectHeader getHeader() {
		return header;
	}
}
