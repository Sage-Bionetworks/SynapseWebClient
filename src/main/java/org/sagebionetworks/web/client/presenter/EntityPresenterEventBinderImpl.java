package org.sagebionetworks.web.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class EntityPresenterEventBinderImpl implements EntityPresenterEventBinder {

	/** Event binder code **/
	interface EntityViewBinder extends EventBinder<EntityPresenter> {};
	private final EntityViewBinder eventBinder = GWT.create(EntityViewBinder.class);
	@Override
	public EventBinder<EntityPresenter> getEventBinder() {
		return eventBinder;
	}
}
