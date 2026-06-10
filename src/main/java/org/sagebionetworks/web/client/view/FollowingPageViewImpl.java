package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class FollowingPageViewImpl
  extends Composite
  implements FollowingPageView {

  @UiField
  ReactComponent reactContainer;

  Header headerWidget;

  public interface LoginViewImplBinder
    extends UiBinder<Widget, FollowingPageViewImpl> {}

  private final LoginViewImplBinder uiBinder = GWT.create(
    LoginViewImplBinder.class
  );

  @Inject
  public FollowingPageViewImpl(Header headerWidget) {
    initWidget(uiBinder.createAndBindUi(this));
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
    ReactElement element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SubscriptionPage,
      EmptyProps.create()
    );
    reactContainer.render(element);
  }
}
