package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;

public class EvaluationFinderViewImpl implements EvaluationFinderView {

  public interface Binder extends UiBinder<Widget, EvaluationFinderViewImpl> {}

  private static Binder uiBinder = GWT.create(Binder.class);

  @UiField
  Modal modal;

  @UiField
  Div synAlertContainer;

  @UiField
  Div paginationWidgetContainer;

  @UiField
  Button selectButton;

  @UiField
  Button cancelButton;

  @UiField
  Div evaluationListContainer;

  private Presenter presenter;

  Widget widget;

  public EvaluationFinderViewImpl() {
    widget = uiBinder.createAndBindUi(this);
    cancelButton.addClickHandler(event -> {
      modal.hide();
    });
    selectButton.addClickHandler(event -> {
      presenter.onOk();
    });
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setEvaluationList(IsWidget w) {
    evaluationListContainer.clear();
    evaluationListContainer.add(w);
  }

  @Override
  public void setPaginationWidget(IsWidget w) {
    paginationWidgetContainer.clear();
    paginationWidgetContainer.add(w);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    modal.show();
  }

  @Override
  public void hide() {
    modal.hide();
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }
}
