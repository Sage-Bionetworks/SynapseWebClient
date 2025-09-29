package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.FeatureFlagConfig;
import org.sagebionetworks.web.client.FeatureFlagKey;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.jsinterop.EntityTreeTableProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class FilesBrowserViewImpl implements FilesBrowserView {

  public interface FilesBrowserViewImplUiBinder
    extends UiBinder<Widget, FilesBrowserViewImpl> {}

  private EntityTreeBrowser entityTreeBrowser;
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
  FeatureFlagConfig featureFlagConfig;
  PortalGinInjector ginInjector;

  @Inject
  public FilesBrowserViewImpl(
    FilesBrowserViewImplUiBinder binder,
    FeatureFlagConfig featureFlagConfig,
    PortalGinInjector ginInjector
  ) {
    widget = binder.createAndBindUi(this);
    this.featureFlagConfig = featureFlagConfig;
    this.ginInjector = ginInjector;
  }

  @Override
  public void configure(String entityId) {
    title.setVisible(false);
    files.setVisible(true);

    files.clear();
    // TODO: clean the following block when EntityTreeTable is released
    if (featureFlagConfig.isFeatureEnabled(FeatureFlagKey.ENTITY_TREE_TABLE)) {
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
    } else {
      if (entityTreeBrowser == null) {
        this.entityTreeBrowser = ginInjector.getEntityTreeBrowser();
        entityTreeBrowser.asWidget().addStyleName("margin-top-10");
      }
      files.add(entityTreeBrowser.asWidget());
      entityTreeBrowser.configure(entityId);
    }
  }

  @Override
  public void setEntityClickedHandler(CallbackP<String> callback) {
    this.entityClickedCallback = callback;
    // TODO: remove the following block when EntityTreeTable is released
    if (entityTreeBrowser != null) {
      entityTreeBrowser.setEntityClickedHandler(entityId -> {
        entityTreeBrowser.setLoadingVisible(true);
        entityClickedCallback.invoke(entityId);
      });
    }
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
    if (entityTreeBrowser != null) {
      entityTreeBrowser.clear();
    }
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
