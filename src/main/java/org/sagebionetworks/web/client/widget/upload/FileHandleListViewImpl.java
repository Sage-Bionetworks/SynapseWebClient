package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.widget.CheckBoxState;
import org.sagebionetworks.web.client.widget.SelectionOptions;
import org.sagebionetworks.web.client.widget.SelectionToolbar;

public class FileHandleListViewImpl implements FileHandleListView {

  public interface Binder extends UiBinder<Widget, FileHandleListViewImpl> {}

  Widget widget;
  Presenter presenter;

  @UiField
  SelectionOptions selectionOptions;

  @UiField
  TBody fileLinksContainer;

  @UiField
  Div fileUploadContainer;

  @Inject
  public FileHandleListViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);

    selectionOptions.setDeleteClickedCallback(event -> {
      presenter.deleteSelected();
    });
    selectionOptions.setSelectAllClicked(event -> {
      presenter.selectAll();
    });
    selectionOptions.setSelectNoneClicked(event -> {
      presenter.selectNone();
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setUploadWidget(Widget widget) {
    fileUploadContainer.clear();
    fileUploadContainer.add(widget);
  }

  @Override
  public void setUploadWidgetVisible(boolean visible) {
    fileUploadContainer.setVisible(visible);
  }

  @Override
  public void addFileLink(Widget fileLinkWidget) {
    fileLinksContainer.add(fileLinkWidget);
  }

  @Override
  public void clearFileLinks() {
    fileLinksContainer.clear();
  }

  @Override
  public void setToolbarVisible(boolean visible) {
    selectionOptions.setVisible(visible);
  }

  @Override
  public void setCanDelete(boolean canDelete) {
    selectionOptions.setCanDelete(canDelete);
  }
}
