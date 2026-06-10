package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.jsinterop.EvaluationCardProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class AdministerEvaluationsListViewImpl
  implements AdministerEvaluationsListView {

  public interface Binder
    extends UiBinder<Widget, AdministerEvaluationsListViewImpl> {}

  @UiField
  Div rows;

  @UiField
  Div widgetsContainer;

  Widget widget;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public AdministerEvaluationsListViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void clearRows() {
    rows.clear();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void add(IsWidget w) {
    widgetsContainer.add(w);
  }

  @Override
  public void addReactComponent(
    Evaluation evaluation,
    EvaluationCardProps props
  ) {
    ReactComponent container = new ReactComponent();
    container.addStyleName("margin-top-50");
    rows.add(container);

    ReactElement element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EvaluationCard,
      props
    );
    container.render(element);
  }
}
