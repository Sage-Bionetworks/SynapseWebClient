package org.sagebionetworks.web.client.widget.entity.browse;

import com.google.gwt.core.client.GWT;
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
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityFileBrowserProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class FilesBrowserViewImpl implements FilesBrowserView {

  public interface FilesBrowserViewImplUiBinder
    extends UiBinder<Widget, FilesBrowserViewImpl> {}

  private EntityTreeBrowser entityTreeBrowser;
  private Widget widget;
  SynapseReactClientFullContextPropsProvider propsProvider;
  private FeatureFlagConfig featureFlagConfig;

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

  @UiField
  Div reactFileBrowserContainer;

  CallbackP<String> entityClickedCallback;

  @Inject
  public FilesBrowserViewImpl(
    FilesBrowserViewImplUiBinder binder,
    EntityTreeBrowser entityTreeBrowser,
    SynapseReactClientFullContextPropsProvider propsProvider,
    FeatureFlagConfig featureFlagConfig
  ) {
    widget = binder.createAndBindUi(this);
    this.featureFlagConfig = featureFlagConfig;
    this.propsProvider = propsProvider;
    this.entityTreeBrowser = entityTreeBrowser;
    Widget etbW = entityTreeBrowser.asWidget();
    etbW.addStyleName("margin-top-10");
    files.add(etbW);
  }

  @Override
  public void configure(String entityId) {
    boolean isReactFileBrowser = featureFlagConfig.isFeatureEnabled(
      FeatureFlagKey.REACT_FILE_BROWSER
    );
    title.setVisible(false);
    reactFileBrowserContainer.setVisible(isReactFileBrowser);
    files.setVisible(!isReactFileBrowser);
    if (isReactFileBrowser) {
      rerenderFileBrowser(entityId);
    } else {
      entityTreeBrowser.configure(entityId);
    }
  }

  public void rerenderFileBrowser(String parentContainerId) {
    // TODO: Should not need to remount, but currently the parentContainer is not shown if the existing react component is updated
    // TODO: Bug: The tree is not always auto-expanding (to the path to the parent container Id)
    reactFileBrowserContainer.clear();
    ReactComponent componentContainer = new ReactComponent();
    EntityFileBrowserProps props = EntityFileBrowserProps.create(
      parentContainerId,
      ref -> {
        String entityId = ref.getTargetId();
        entityClickedCallback.invoke(entityId);
      }
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityFileBrowser,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactFileBrowserContainer.add(componentContainer);
    componentContainer.render(component);
  }

  @Override
  public void setEntityClickedHandler(CallbackP<String> callback) {
    this.entityClickedCallback = callback;
    entityTreeBrowser.setEntityClickedHandler(entityId -> {
      entityTreeBrowser.setLoadingVisible(true);
      entityClickedCallback.invoke(entityId);
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
