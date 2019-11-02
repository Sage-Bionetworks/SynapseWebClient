package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.client.widget.SelectionToolbar;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class UserBadgeListViewImpl implements UserBadgeListView {

	public interface Binder extends UiBinder<Widget, UserBadgeListViewImpl> {
	}

	Widget widget;
	Presenter presenter;

	@UiField
	SelectionToolbar selectionToolbar;
	@UiField
	Div userBadgeContainer;

	@Inject
	public UserBadgeListViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);

		selectionToolbar.hideReordering();

		selectionToolbar.setDeleteClickedCallback(event -> {
			presenter.deleteSelected();
		});
		selectionToolbar.setSelectAllClicked(event -> {
			presenter.selectAll();
		});
		selectionToolbar.setSelectNoneClicked(event -> {
			presenter.selectNone();
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addUserBadge(Widget user) {
		userBadgeContainer.add(user.asWidget());
	}

	@Override
	public void clearUserBadges() {
		userBadgeContainer.clear();
	}

	@Override
	public void setToolbarVisible(boolean visible) {
		selectionToolbar.setVisible(visible);
	}

	@Override
	public void setCanDelete(boolean canDelete) {
		selectionToolbar.setCanDelete(canDelete);
	}

	@Override
	public void setSelectionState(CheckBoxState selectionState) {
		selectionToolbar.setSelectionState(selectionState);
	}
}
