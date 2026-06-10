package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Span;

public class TeamMemberCountViewImpl implements TeamMemberCountView {

  public interface Binder extends UiBinder<Widget, TeamMemberCountViewImpl> {}

  @UiField
  Span countContainer;

  @UiField
  Span synAlertContainer;

  Widget widget;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public TeamMemberCountViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setCount(String count) {
    countContainer.setText(count);
  }

  @Override
  public void setSynAlert(Widget widget) {
    synAlertContainer.clear();
    synAlertContainer.add(widget);
  }
}
