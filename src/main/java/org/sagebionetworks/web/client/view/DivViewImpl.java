package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;

public class DivViewImpl extends Div implements DivView {

  @Override
  public void setText(String text) {
    add(new Text(text));
  }
}
