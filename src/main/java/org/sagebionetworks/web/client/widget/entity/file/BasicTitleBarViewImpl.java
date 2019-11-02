package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BasicTitleBarViewImpl implements BasicTitleBarView {

	private Presenter presenter;

	@UiField
	Span fileName;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	Icon entityIcon;

	interface BasicTitleBarViewImplUiBinder extends UiBinder<Widget, BasicTitleBarViewImpl> {
	}

	private static BasicTitleBarViewImplUiBinder uiBinder = GWT.create(BasicTitleBarViewImplUiBinder.class);
	Widget widget;

	@Inject
	public BasicTitleBarViewImpl() {
		widget = uiBinder.createAndBindUi(this);
	}

	public void setFavoritesWidget(Widget favoritesWidget) {
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoritesWidget);
	};

	@Override
	public void setFavoritesWidgetVisible(boolean visible) {
		favoritePanel.setVisible(visible);
	}

	@Override
	public void setTitle(String name) {
		fileName.setText(name);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setIconType(IconType iconType) {
		entityIcon.setType(iconType);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
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
}
