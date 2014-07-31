package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadge implements EntityBadgeView.Presenter, SynapseWidgetPresenter {
	
	private EntityBadgeView view;
	private EntityIconsCache iconsCache;
	
	@Inject
	public EntityBadge(EntityBadgeView view, EntityIconsCache iconsCache) {
		this.view = view;
		this.iconsCache = iconsCache;
		view.setPresenter(this);
	}
	
	public void configure(EntityHeader header) {
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

}
