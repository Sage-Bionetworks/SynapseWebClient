package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;

public class EntityListWidgetViewImpl implements EntityListWidgetView {

  public interface Binder extends UiBinder<Widget, EntityListWidgetViewImpl> {}

  Widget widget;

  @UiField
  Panel table;

  @UiField
  Div rows;

  @UiField
  Span emptyUI;

  @UiField
  TableHeader descriptionHeader;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public EntityListWidgetViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void addRow(Widget w) {
    rows.add(w);
  }

  @Override
  public void clearRows() {
    rows.clear();
  }

  @Override
  public void setEmptyUiVisible(boolean visible) {
    emptyUI.setVisible(visible);
  }

  @Override
  public void setTableVisible(boolean visible) {
    table.setVisible(visible);
  }

  @Override
  public void setDescriptionHeaderVisible(boolean visible) {
    descriptionHeader.setVisible(visible);
  }
}
