package org.sagebionetworks.web.client.widget.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Breadcrumb implements BreadcrumbView.Presenter,
		SynapseWidgetPresenter {

	private BreadcrumbView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private EntityTypeProvider entityTypeProvider;

	@Inject
	public Breadcrumb(BreadcrumbView view, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator,
			EntityTypeProvider entityTypeProvider) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.entityTypeProvider = entityTypeProvider;

		view.setPresenter(this);
	}

	/**
	 * Create Breadcrumbs for an Entity
	 * 
	 * @param entity
	 * @return
	 */
	public Widget asWidget(EntityPath entityPath) {
		view.setPresenter(this);
		// exchange root for home
		List<LinkData> links = new ArrayList<LinkData>();
		links.add(new LinkData("Home", new Home(
				DisplayUtils.DEFAULT_PLACE_TOKEN)));
		String current = null;
		if (entityPath != null) {
			List<EntityHeader> path = entityPath.getPath();
			if (path != null) {
				// create link data for each path element except for the first
				// (root) and last (current)
				for (int i = 1; i < path.size() - 1; i++) {
					EntityHeader element = path.get(i);
					links.add(new LinkData(element.getName(), new Synapse(
							element.getId())));
				}
				// set current as name of last path element
				current = path.get(path.size() - 1).getName();
			}
		}
		view.setLinksList(links, current);
		return view.asWidget();
	}
	/**
	 * Use when the only page link is back to the Home page 
	 * @param currentPageName
	 * @return
	 */
	public Widget asWidget(String currentPageName){
		List<LinkData> links = new ArrayList<LinkData>();
		links.add(new LinkData("Home", new Home(
				DisplayUtils.DEFAULT_PLACE_TOKEN)));
		return asWidget(links, currentPageName);
	}
	
	/**
	 * Create Breadcrumbs for an arbitrary set of link data, ending in the current page name
	 * @param links
	 * @param currentPageName
	 * @return
	 */
	public Widget asWidget(List<LinkData> links, String currentPageName){
		view.setPresenter(this);
		view.setLinksList(links, currentPageName);
		return view.asWidget();
	}
	
	/**
	 * Not used
	 */
	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

}
