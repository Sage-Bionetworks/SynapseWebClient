package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.WidgetConstants;

public class ReferenceWidgetViewImpl
  extends FlowPanel
  implements ReferenceWidgetView {

  private String id;
  private SynapseJSNIUtils jsniUtils;

  @Inject
  public ReferenceWidgetViewImpl(SynapseJSNIUtils jsniUtils) {
    this.jsniUtils = jsniUtils;
  }

  @Override
  public void configure(String footnoteId) {
    this.clear();
    id = footnoteId;

    Anchor a = new Anchor();
    a.setHTML("[" + id + "]");
    a.addStyleName("link margin-left-5");
    a.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          HTMLPanel parentPanel = (HTMLPanel) getParent();
          Element heading = parentPanel.getElementById(
            WidgetConstants.FOOTNOTE_ID_WIDGET_PREFIX + id
          );
          final Element scrollToElement = heading;
          jsniUtils.scrollIntoView(scrollToElement);
        }
      }
    );
    add(a);
  }

  @Override
  public Widget asWidget() {
    return this;
  }
}
