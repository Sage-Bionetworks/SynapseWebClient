package org.sagebionetworks.web.client.widget.breadcrumb;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Breadcrumb implements BreadcrumbView.Presenter, SynapseWidgetPresenter, IsWidget {

	private BreadcrumbView view;
	private GlobalApplicationState globalApplicationState;
	private CallbackP<Place> callback;

	@Inject
	public Breadcrumb(BreadcrumbView view, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}

	/**
	 * Create Breadcrumbs for an Entity
	 * 
	 * @param entity
	 */
	public void configure(EntityPath entityPath, EntityArea optionalArea) {
		view.setPresenter(this);
		List<LinkData> links = new ArrayList<LinkData>();
		String currentPageName = null;
		if (entityPath != null) {
			List<EntityHeader> path = entityPath.getPath();
			if (path != null) {
				// create link data for each path element except for the first
				// (root) and last (current)
				for (int i = 1; i < path.size() - 1; i++) {
					EntityHeader element = path.get(i);
					String name = element.getName();
					Synapse place = new Synapse(element.getId());
					IconType icon = EntityTypeUtils.getIconTypeForEntityClassName(element.getType());
					if (optionalArea == EntityArea.FILES && Project.class.getName().equals(element.getType())) {
						// show files as root
						name = DisplayConstants.FILES;
						place.setArea(EntityArea.FILES);
					} else if (optionalArea == EntityArea.TABLES && Project.class.getName().equals(element.getType())) {
						// show tables as root
						name = DisplayConstants.TABLES;
						place.setArea(EntityArea.TABLES);
					}
					links.add(new LinkData(name, icon, place));
				}
				currentPageName = path.get(path.size() - 1).getName();
			}
		}
		if (currentPageName != null) {
			view.setLinksList(links, currentPageName);
		} else {
			view.setLinksList(links);
		}
	}

	/**
	 * Create Breadcrumbs for an arbitrary set of link data, ending in the current page name
	 * 
	 * @param links
	 * @param currentPageName
	 */
	public void configure(List<LinkData> links, String currentPageName) {
		view.setPresenter(this);
		view.setLinksList(links, currentPageName);
	}


	@Override
	public void clear() {
		view.clear();
	}

	/**
	 * Not used
	 */
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setLinkClickedHandler(CallbackP<Place> callback) {
		this.callback = callback;
	}

	@Override
	public void goTo(Place place) {
		if (callback == null) {
			globalApplicationState.getPlaceChanger().goTo(place);
		} else {
			callback.invoke(place);
		}

	}

}
