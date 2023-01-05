package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EntityPageTitleBarProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class BasicTitleBarViewImpl implements BasicTitleBarView {

  private final SynapseContextPropsProvider propsProvider;

  @UiField
  ReactComponentDiv reactComponentContainer;

  @Override
  public void setProps(EntityPageTitleBarProps props) {
    ReactNode reactNode = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntityPageTitleBar,
      props,
      propsProvider.getJsInteropContextProps()
    );
    reactComponentContainer.render(reactNode);
  }

  interface BasicTitleBarViewImplUiBinder
    extends UiBinder<Widget, BasicTitleBarViewImpl> {}

  private static BasicTitleBarViewImplUiBinder uiBinder = GWT.create(
    BasicTitleBarViewImplUiBinder.class
  );
  Widget widget;

  @Inject
  public BasicTitleBarViewImpl(SynapseContextPropsProvider propsProvider) {
    widget = uiBinder.createAndBindUi(this);
    this.propsProvider = propsProvider;
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
}
