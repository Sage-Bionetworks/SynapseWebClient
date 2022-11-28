package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.HelpWidget;

public class FilesBrowserViewImpl implements FilesBrowserView {

  public interface FilesBrowserViewImplUiBinder
    extends UiBinder<Widget, FilesBrowserViewImpl> {}

  private EntityTreeBrowser entityTreeBrowser;
  private Widget widget;

  @UiField
  Div files;

  @UiField
  Div commandsContainer;

  @UiField
  Div addToDownloadListContainer;

  @UiField
  Div actionMenuContainer;

  @UiField
  Heading title;

  @Inject
  public FilesBrowserViewImpl(
    FilesBrowserViewImplUiBinder binder,
    EntityTreeBrowser entityTreeBrowser
  ) {
    widget = binder.createAndBindUi(this);
    this.entityTreeBrowser = entityTreeBrowser;
    Widget etbW = entityTreeBrowser.asWidget();
    etbW.addStyleName("margin-top-10");
    files.add(etbW);
  }

  @Override
  public void configure(String entityId) {
    title.setVisible(false);
    entityTreeBrowser.configure(entityId);
  }

  @Override
  public void setEntityClickedHandler(CallbackP<String> callback) {
    entityTreeBrowser.setEntityClickedHandler(entityId -> {
      entityTreeBrowser.setLoadingVisible(true);
      callback.invoke(entityId);
    });
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {
    entityTreeBrowser.clear();
  }

  @Override
  public void setActionMenu(IsWidget w) {
    w.asWidget().removeFromParent();
    actionMenuContainer.clear();
    actionMenuContainer.add(w);
    // if showing action menu, then show title.
    title.setVisible(true);
  }

  @Override
  public void setAddToDownloadListWidget(IsWidget w) {
    w.asWidget().removeFromParent();
    addToDownloadListContainer.clear();
    addToDownloadListContainer.add(w);
  }
}
