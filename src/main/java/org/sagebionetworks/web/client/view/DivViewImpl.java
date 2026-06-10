package org.sagebionetworks.web.client.view;

import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;

public class DivViewImpl extends Div implements DivView {

  @Inject
  public DivViewImpl() {}

  @Override
  public void setText(String text) {
    add(new Text(text));
  }
}
