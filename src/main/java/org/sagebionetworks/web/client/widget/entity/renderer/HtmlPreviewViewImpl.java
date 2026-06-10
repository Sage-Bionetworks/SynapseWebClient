package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.jsinterop.HtmlPreviewProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class HtmlPreviewViewImpl implements HtmlPreviewView {

  public interface Binder extends UiBinder<Widget, HtmlPreviewViewImpl> {}

  @UiField
  Div synAlertContainer;

  @UiField
  Div loadingUI;

  @UiField
  ReactComponent container;

  Widget w;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public HtmlPreviewViewImpl() {
    w = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void configure(String createdBy, String rawHtml) {
    HtmlPreviewProps props = HtmlPreviewProps.create(createdBy, rawHtml);

    ReactElement element = React.createElementWithSynapseContext(
      SRC.SynapseComponents.HtmlPreview,
      props
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
