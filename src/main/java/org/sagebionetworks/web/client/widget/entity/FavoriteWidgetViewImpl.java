package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

public class FavoriteWidgetViewImpl implements FavoriteWidgetView {

  public interface Binder extends UiBinder<Widget, FavoriteWidgetViewImpl> {}

  private Presenter presenter;

  @UiField
  Span favWidgetContainer;

  @UiField
  Anchor favoriteIcon;

  @UiField
  Anchor notFavoriteIcon;

  @UiField
  LoadingSpinner loadingUI;

  private Widget widget;

  @Inject
  public FavoriteWidgetViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);
    favoriteIcon.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.favoriteClicked();
        }
      }
    );
    notFavoriteIcon.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.favoriteClicked();
        }
      }
    );
    favoriteIcon
      .getElement()
      .setAttribute(
        "aria-label",
        "Click the star to remove this from your favorites"
      );
    notFavoriteIcon
      .getElement()
      .setAttribute(
        "aria-label",
        "Click the star to add this to your favorites"
      );
  }

  @Override
  public void setFavoriteVisible(boolean isVisible) {
    favoriteIcon.setVisible(isVisible);
  }

  @Override
  public void setNotFavoriteVisible(boolean isVisible) {
    notFavoriteIcon.setVisible(isVisible);
  }

  @Override
  public void setLoadingVisible(boolean isVisible) {
    loadingUI.setVisible(isVisible);
  }

  @Override
  public void setFavWidgetContainerVisible(boolean isVisible) {
    favWidgetContainer.setVisible(isVisible);
  }

  @Override
  public void setPresenter(Presenter p) {
    presenter = p;
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setLoadingSize(int px) {
    loadingUI.setSize(px);
    loadingUI.setMarginLeft(px / 2);
  }
}
