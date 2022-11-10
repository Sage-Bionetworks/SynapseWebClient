package org.sagebionetworks.web.client.view.bootstrap.table;

import com.google.gwt.dom.client.Document;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;

/**
 * Simple <thead>
 *
 * @author jmhill
 *
 */
public class THead extends ComplexWidget {

  public THead() {
    setElement(Document.get().createTHeadElement());
  }
}
