package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Anchor;

public class LinkCellRendererViewImpl
  extends Anchor
  implements LinkCellRendererView {

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void setValue(String value) {
    this.setHref(value);
    this.setText(value);
    this.setTarget(BLANK);
  }

  @Override
  public String getValue() {
    return this.getText();
  }
}
