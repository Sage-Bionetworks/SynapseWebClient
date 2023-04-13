package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.HtmlPreviewProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class HtmlPreviewViewImpl implements HtmlPreviewView {

  public interface Binder extends UiBinder<Widget, HtmlPreviewViewImpl> {}

  @UiField
  Div synAlertContainer;

  @UiField
  Div loadingUI;

  @UiField
  ReactComponentDiv container;

  Widget w;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public HtmlPreviewViewImpl(
    Binder binder,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    w = binder.createAndBindUi(this);
    this.propsProvider = propsProvider;
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void configure(String createdBy, String rawHtml) {
    HtmlPreviewProps props = HtmlPreviewProps.create(createdBy, rawHtml);

    ReactNode element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.HtmlPreview,
      props,
      propsProvider.getJsInteropContextProps()
    );

    container.render(element);
  }

  @Override
  public void setLoadingVisible(boolean visible) {
    loadingUI.setVisible(visible);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }
}
