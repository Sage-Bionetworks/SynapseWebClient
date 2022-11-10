package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Strong;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeaderResizeGrip;

/**
 * This is a view only component that contains zero business logic.
 *
 * @author jhill
 *
 */

public class SortableTableHeaderImpl implements SortableTableHeader {

  public static final String UNSORTED_STYLES = "synapse-blue";
  public static final String SORTED_STYLES = "synapse-blue-bg color-white";

  public interface Binder extends UiBinder<Widget, SortableTableHeaderImpl> {}

  @UiField
  Strong header;

  @UiField
  TableHeaderResizeGrip resizeGrip;

  @UiField
  Icon sortIcon;

  Widget widget;
  private static Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public SortableTableHeaderImpl() {
    widget = uiBinder.createAndBindUi(this);
    setSortDirection(null);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void configure(final String text, final SortingListener handler) {
    header.setText(text);
    setSortingListener(handler);
  }

  public void setSortingListener(final SortingListener handler) {
    if (handler != null) {
      ClickHandler onClick = event -> {
        handler.onToggleSort(getText());
      };
      sortIcon.addClickHandler(onClick);
    }
  }

  @Override
  public void setSortDirection(SortDirection direction) {
    IconType icon = IconType.SYN_SORT_DESC;
    if (direction == null) {
      sortIcon.removeStyleName(SORTED_STYLES);
      sortIcon.addStyleName(UNSORTED_STYLES);
    } else {
      sortIcon.removeStyleName(UNSORTED_STYLES);
      sortIcon.addStyleName(SORTED_STYLES);
      if (SortDirection.ASC.equals(direction)) {
        icon = IconType.SYN_SORT_ASC;
      }
    }
    sortIcon.setType(icon);
  }

  public void setWidth(String width) {
    widget.setWidth(width);
  }

  public void setHeight(String height) {
    widget.setHeight(height);
  }

  public void setAddStyleNames(String styles) {
    widget.addStyleName(styles);
  }

  public void setStyleName(String styles) {
    widget.setStyleName(styles);
  }

  public void setText(String text) {
    header.setText(text);
  }

  public String getText() {
    return header.getText();
  }

  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }

  @Override
  public void setIsResizable(boolean isResizable) {
    resizeGrip.setVisible(isResizable);
  }
}
