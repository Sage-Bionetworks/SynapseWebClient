package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Span;

public class TeamSubjectWidgetViewImpl implements TeamSubjectWidgetView {

  private Presenter presenter;

  public interface Binder extends UiBinder<Widget, TeamSubjectWidgetViewImpl> {}

  @UiField
  Span container;

  @UiField
  Button deleteButton;

  Widget w;

  @Inject
  public TeamSubjectWidgetViewImpl(Binder binder) {
    this.w = binder.createAndBindUi(this);
    deleteButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.onDelete();
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setSubjectRendererWidget(IsWidget w) {
    container.clear();
    container.add(w);
  }

  @Override
  public void setDeleteVisible(boolean visible) {
    deleteButton.setVisible(visible);
  }
}
