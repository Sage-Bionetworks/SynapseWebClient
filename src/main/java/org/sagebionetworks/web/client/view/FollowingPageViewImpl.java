package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

public class FollowingPageViewImpl
  extends Composite
  implements FollowingPageView {

  @UiField
  ReactComponentDiv reactContainer;

  Header headerWidget;

  SynapseReactClientFullContextPropsProvider propsProvider;

  public interface LoginViewImplBinder
    extends UiBinder<Widget, FollowingPageViewImpl> {}

  @Inject
  public FollowingPageViewImpl(
    LoginViewImplBinder uiBinder,
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(uiBinder.createAndBindUi(this));
    this.propsProvider = propsProvider;
    this.headerWidget = headerWidget;
    configure();
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
    reactContainer.clear();
    configure();
  }

  private void configure() {
    headerWidget.configure();
    ReactNode element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SubscriptionPage,
      EmptyProps.create(),
      propsProvider.getJsInteropContextProps()
    );
    reactContainer.render(element);
  }
}
