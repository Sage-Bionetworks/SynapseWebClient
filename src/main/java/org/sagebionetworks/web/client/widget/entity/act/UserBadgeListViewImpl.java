package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.SelectionOptions;

public class UserBadgeListViewImpl implements UserBadgeListView {

  public interface Binder extends UiBinder<Widget, UserBadgeListViewImpl> {}

  Widget widget;
  Presenter presenter;

  @UiField
  SelectionOptions selectionOptions;

  @UiField
  Div userBadgeContainer;

  @Inject
  public UserBadgeListViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);

    selectionOptions.setDeleteClickedCallback(event -> {
      presenter.deleteSelected();
    });
    selectionOptions.setSelectAllClicked(event -> {
      presenter.selectAll();
    });
    selectionOptions.setSelectNoneClicked(event -> {
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
  public void setSelectionOptionsVisible(boolean visible) {
    selectionOptions.setVisible(visible);
  }

  @Override
  public void setCanDelete(boolean canDelete) {
    selectionOptions.setCanDelete(canDelete);
  }
}
