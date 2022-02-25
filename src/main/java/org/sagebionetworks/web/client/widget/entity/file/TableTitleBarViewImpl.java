package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableTitleBarViewImpl extends Composite implements TableTitleBarView {
	Presenter presenter;
	private FavoriteWidget favoriteWidget;
	AuthenticationController authController;
	@UiField
	HTMLPanel panel;
	@UiField
	HTMLPanel fileFoundContainer;
	@UiField
	EntityTypeIcon entityIcon;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	Heading entityName;
	@UiField
	Anchor showVersionHistoryLink;
	@UiField
	Span versionUiCurrent;

	interface TableTitleBarViewImplUiBinder extends UiBinder<Widget, TableTitleBarViewImpl> {
	}

	private String currentEntityId;
	private static TableTitleBarViewImplUiBinder uiBinder = GWT.create(TableTitleBarViewImplUiBinder.class);

	@Inject
	public TableTitleBarViewImpl(FavoriteWidget favoriteWidget, AuthenticationController authController) {
		this.favoriteWidget = favoriteWidget;
		this.authController = authController;

		initWidget(uiBinder.createAndBindUi(this));

		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		showVersionHistoryLink.addClickHandler(event -> {
			boolean isShown = !presenter.isVersionHistoryVisible();
			presenter.toggleShowVersionHistory();
			showVersionHistoryLink.setText((isShown ? "Hide" : "Show") + " Version History");
		});

	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}

	@Override
	public void createTitlebar(Entity entity) {
		currentEntityId = entity.getId();
		favoriteWidget.configure(currentEntityId);
		entityIcon.setType(EntityTypeUtils.getEntityType(entity));
	}

	@Override
	public void setEntityName(String name) {
		entityName.setText(name);
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
	public void setVersionLabel(String label) {
		versionUiCurrent.setText(label);
	}

}
