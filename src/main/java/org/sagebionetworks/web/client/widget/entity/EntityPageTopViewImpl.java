package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;

public class EntityPageTopViewImpl extends Composite implements EntityPageTopView {

	public interface Binder extends UiBinder<Widget, EntityPageTopViewImpl> {
	}

	@UiField
	Div tabsUI;
	@UiField
	LoadingSpinner loadingUI;

	// project level info
	@UiField
	SimplePanel projectMetadataContainer;
	@UiField
	Span projectActionMenuContainer;
	@UiField
	Span entityActionMenuContainer;

	@Inject
	public EntityPageTopViewImpl(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setProjectActionMenu(Widget w) {
		projectActionMenuContainer.add(w);
	}

	@Override
	public void setEntityActionMenu(Widget w) {
		entityActionMenuContainer.add(w);
	}

	@Override
	public void setProjectMetadata(Widget w) {
		projectMetadataContainer.setWidget(w);
	}


	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public void setTabs(Widget w) {
		tabsUI.clear();
		tabsUI.add(w);
	}

	@Override
	public void setProjectLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}

	@Override
	public void scrollToTop() {
		DisplayUtils.scrollToTop();
	}

	/** Event binder code **/
	interface EBinder extends EventBinder<EntityPageTop> {
	};

	private final EBinder eventBinder = GWT.create(EBinder.class);

	@Override
	public EventBinder<EntityPageTop> getEventBinder() {
		return eventBinder;
	}
}
