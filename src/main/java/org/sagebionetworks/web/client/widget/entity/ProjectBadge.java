package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.KeyValueDisplay;

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
	private FavoriteWidget favoritesWidget;
	
	@Inject
	public ProjectBadge(ProjectBadgeView view, 
			SynapseClientAsync synapseClient,
			AdapterFactory adapterFactory,
			GlobalApplicationState globalAppState,
			ClientCache clientCache,
			FavoriteWidget favoritesWidget
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.globalAppState = globalAppState;
		this.clientCache = clientCache;
		this.favoritesWidget = favoritesWidget;
		view.setPresenter(this);
		view.setFavoritesWidget(favoritesWidget.asWidget());
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
			favoritesWidget.configure(header.getId());
			view.setProject(header.getName(), DisplayUtils.getSynapseHistoryToken(header.getId()));
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}	
	
	public KeyValueDisplay<String> profileToKeyValueDisplay(UserProfile profile, String modifiedBy) {
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
		
		order.add("ID");
		map.put("ID", header.getId());

		order.add("Modified By");
		map.put("Modified By", modifiedBy);
		
		order.add("Modified On");

		if (header.getModifiedOn() != null)
			map.put("Modified On", DisplayUtils.converDataToPrettyString(header.getModifiedOn()));		
		
		return new KeyValueDisplay<String>(map, order);
	}
	
	@Override
	public void getInfo(final AsyncCallback<KeyValueDisplay<String>> callback) {
		UserBadge.getUserProfile("" + header.getModifiedBy(), adapterFactory, synapseClient, clientCache, new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile profile) {
				if (view.isAttached())
					callback.onSuccess(profileToKeyValueDisplay(profile, DisplayUtils.getDisplayName(profile)));		
			}

			@Override
			public void onFailure(Throwable caught) {
				if (view.isAttached())
					callback.onFailure(caught);
			}
		});
	}
	
	public ProjectHeader getHeader() {
		return header;
	}
}
