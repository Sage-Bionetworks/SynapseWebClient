package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.PortalGinInjector;

/**
 * A non editable renderer for a list of entity IDs
 *
 */
public class EntityIdListRendererCellViewImpl
  implements EntityIdListRendererCellView {

  String v;
  JSONObjectAdapter adapter;
  PortalGinInjector ginInjector;
  Div div = new Div();

  @Inject
  public EntityIdListRendererCellViewImpl(
    JSONObjectAdapter adapter,
    PortalGinInjector ginInjector
  ) {
    super();
    this.adapter = adapter;
    this.ginInjector = ginInjector;
    div.addStyleName("whitespace-nowrap");
    div.addAttachHandler(event -> {
      if (event.isAttached()) {
        // div has been attached.  add the "truncate" style to it's parent (td)
        div.getParent().addStyleName("truncate");
      }
    });
  }

  @Override
  public void setValue(String jsonValue) {
    this.v = jsonValue;
    div.clear();
    if (v != null) {
      // try to parse out json values
      try {
        JSONArrayAdapter parsedJson = adapter.createNewArray(jsonValue);
        int arrayLength = parsedJson.length();
        for (int i = 0; i < arrayLength; i++) {
          String entityId = parsedJson.get(i).toString();
          EntityIdCellRenderer renderer = ginInjector.createEntityIdCellRenderer();
          renderer.setValue(entityId);
          div.add(renderer);
        }
      } catch (Exception e) {
        div.add(new Text(jsonValue));
      }
    }
  }

  @Override
  public String getValue() {
    return v;
  }

  @Override
  public Widget asWidget() {
    return div;
  }
}
