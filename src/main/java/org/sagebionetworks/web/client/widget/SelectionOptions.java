package org.sagebionetworks.web.client.widget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;

/**
 * Reusable view widget that contains the commands to Select All or to Remove Selected
 *
 * @author jayhodgson
 *
 */
public class SelectionOptions implements IsWidget {

  public interface SelectionOptionsUiBinder
    extends UiBinder<Widget, SelectionOptions> {}

  @UiField
  Anchor selectAll;

  @UiField
  Anchor deleteSelected;

  Widget widget;
  ClickHandler selectAllClicked, selectNoneClicked;

  // empty constructor, this widget can be used directly in your ui xml
  public SelectionOptions() {
    SelectionOptionsUiBinder binder = GWT.create(
      SelectionOptionsUiBinder.class
    );
    widget = binder.createAndBindUi(this);

    selectAll.addClickHandler(event -> {
      if (selectAllClicked != null) {
        selectAllClicked.onClick(event);
      }
    });
    deleteSelected.setEnabled(false);
  }

  public void setDeleteClickedCallback(ClickHandler deleteClicked) {
    deleteSelected.addClickHandler(deleteClicked);
  }

  public void setSelectAllClicked(ClickHandler selectAllClicked) {
    this.selectAllClicked = selectAllClicked;
  }

  public void setSelectNoneClicked(ClickHandler selectNoneClicked) {
    this.selectNoneClicked = selectNoneClicked;
  }

  public void setVisible(boolean isVisible) {
    widget.setVisible(isVisible);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  public void setAddStyleNames(String style) {
    widget.addStyleName(style);
  }

  public void setCanDelete(boolean canDelete) {
    deleteSelected.setEnabled(canDelete);
  }
}
