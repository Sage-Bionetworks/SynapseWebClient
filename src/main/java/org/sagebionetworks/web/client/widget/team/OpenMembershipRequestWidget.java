package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

public class OpenMembershipRequestWidget implements IsWidget {

  public interface Binder
    extends UiBinder<Widget, OpenMembershipRequestWidget> {}

  @UiField
  TableData badgeTableData;

  @UiField
  TableData messageTableData;

  @UiField
  TableData createdOnTableData;

  @UiField
  Button acceptButton;

  @UiField
  Button denyButton;

  private Widget widget;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public OpenMembershipRequestWidget() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
