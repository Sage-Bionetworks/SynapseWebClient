package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;

public class LockAccessRequirementWidgetViewImpl
  implements LockAccessRequirementWidgetView {

  @UiField
  Div deleteAccessRequirementContainer;

  @UiField
  Div teamSubjectsWidgetContainer;

  @UiField
  Div entitySubjectsWidgetContainer;

  public interface Binder
    extends UiBinder<Widget, LockAccessRequirementWidgetViewImpl> {}

  Widget w;

  @Inject
  public LockAccessRequirementWidgetViewImpl(Binder binder) {
    this.w = binder.createAndBindUi(this);
  }

  @Override
  public void addStyleNames(String styleNames) {
    w.addStyleName(styleNames);
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void setDeleteAccessRequirementWidget(IsWidget w) {
    deleteAccessRequirementContainer.clear();
    deleteAccessRequirementContainer.add(w);
  }

  @Override
  public void setTeamSubjectsWidget(IsWidget w) {
    teamSubjectsWidgetContainer.clear();
    teamSubjectsWidgetContainer.add(w);
  }

  @Override
  public void setEntitySubjectsWidget(IsWidget entitySubjectsWidget) {
    entitySubjectsWidgetContainer.clear();
    entitySubjectsWidgetContainer.add(w);
  }
}
