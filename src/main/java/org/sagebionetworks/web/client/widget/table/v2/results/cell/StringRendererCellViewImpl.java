package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;

/**
 * A non editable renderer for a string.
 *
 * @author John
 *
 */
public class StringRendererCellViewImpl implements StringRendererCellView {

  Div div = new Div();
  Paragraph p = new Paragraph();
  public static final int LENGTH_REQUIRES_TOOLTIP = 20;

  public StringRendererCellViewImpl() {
    super();
    div.addAttachHandler(event -> {
      if (event.isAttached()) {
        // div has been attached.  add the "truncate" style to it's parent (td)
        div.getParent().addStyleName("truncate");
      }
    });
  }

  @Override
  public void setValue(String value) {
    p.setText(value);
    if (value != null && value.length() > LENGTH_REQUIRES_TOOLTIP) {
      Tooltip tooltip = new Tooltip(p, value);
      tooltip.setContainer("body");
      tooltip.setPlacement(Placement.BOTTOM);
      tooltip.setTrigger(Trigger.HOVER);
      div.add(tooltip);
    } else {
      div.add(p);
    }
  }

  @Override
  public String getValue() {
    return p.getText();
  }

  @Override
  public Widget asWidget() {
    return div;
  }
}
