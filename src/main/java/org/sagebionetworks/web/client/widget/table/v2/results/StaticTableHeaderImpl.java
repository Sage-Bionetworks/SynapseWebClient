package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeaderResizeGrip;

public class StaticTableHeaderImpl implements StaticTableHeader {

  public interface Binder extends UiBinder<Widget, StaticTableHeaderImpl> {}

  @UiField
  Strong header;

  @UiField
  TableHeaderResizeGrip resizeGrip;

  Widget widget;

  @Inject
  public StaticTableHeaderImpl(Binder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void setHeader(String headerText) {
    header.setText(headerText);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setIsResizable(boolean isResizable) {
    resizeGrip.setVisible(isResizable);
  }
}
