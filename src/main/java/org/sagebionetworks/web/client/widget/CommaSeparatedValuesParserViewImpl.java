package org.sagebionetworks.web.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;

public class CommaSeparatedValuesParserViewImpl
  implements CommaSeparatedValuesParserView {

  public interface Binder
    extends UiBinder<Widget, CommaSeparatedValuesParserViewImpl> {}

  private Widget widget;
  private Presenter presenter;

  @UiField
  Div container;

  @UiField
  Button cancelButton;

  @UiField
  Button addButton;

  @UiField
  TextArea commaSeparatedTextBox;

  private final CommaSeparatedValuesParserViewImpl.Binder uiBinder = GWT.create(
    CommaSeparatedValuesParserViewImpl.Binder.class
  );

  @Inject
  public CommaSeparatedValuesParserViewImpl() {
    widget = uiBinder.createAndBindUi(this);
    cancelButton.addClickHandler(clickEvent -> presenter.onCancel());
    addButton.addClickHandler(clickEvent -> presenter.onAdd());
  }

  @Override
  public String getText() {
    return this.commaSeparatedTextBox.getValue();
  }

  @Override
  public void clearTextBox() {
    commaSeparatedTextBox.clear();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void hide() {
    container.setVisible(false);
  }

  @Override
  public void show() {
    container.setVisible(true);
  }
}
