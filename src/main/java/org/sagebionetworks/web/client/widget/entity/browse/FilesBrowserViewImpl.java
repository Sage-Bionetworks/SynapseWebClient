package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.EntityTreeTableProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class FilesBrowserViewImpl implements FilesBrowserView {

  public interface FilesBrowserViewImplUiBinder
    extends UiBinder<Widget, FilesBrowserViewImpl> {}

  private ReactComponent entityTreeTable;
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

  CallbackP<String> entityClickedCallback;

  @Inject
  public FilesBrowserViewImpl(FilesBrowserViewImplUiBinder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void configure(String entityId) {
    title.setVisible(false);
    files.setVisible(true);

    files.clear();
    if (entityTreeTable == null) {
      this.entityTreeTable = new ReactComponent();
    }
    EntityTreeTableProps.Callback callback = id -> {
      if (entityClickedCallback != null) {
        entityClickedCallback.invoke(id);
      }
    };
    EntityTreeTableProps props = EntityTreeTableProps.create(
      entityId,
      callback
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityTreeTable,
      props
    );
    entityTreeTable.render(component);
    files.add(entityTreeTable.asWidget());
  }

  @Override
  public void setEntityClickedHandler(CallbackP<String> callback) {
    this.entityClickedCallback = callback;
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
  public void clear() {}

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
