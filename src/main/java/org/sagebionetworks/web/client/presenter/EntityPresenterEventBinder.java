package org.sagebionetworks.web.client.presenter;

import com.google.web.bindery.event.shared.binder.EventBinder;

public interface EntityPresenterEventBinder {
	EventBinder<EntityPresenter> getEventBinder();
}
