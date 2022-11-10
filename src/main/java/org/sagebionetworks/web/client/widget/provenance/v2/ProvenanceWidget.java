package org.sagebionetworks.web.client.widget.provenance.v2;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ProvenanceWidget implements WidgetRendererPresenter {

  private static final int DEFAULT_HEIGHT = 200;
  private ProvenanceWidgetView view;
  List<Reference> startRefs;

  @Inject
  public ProvenanceWidget(ProvenanceWidgetView view) {
    this.view = view;
  }

  public void configure(Map<String, String> widgetDescriptor) {
    configure(null, widgetDescriptor, null, null);
  }

  @Override
  public void configure(
    WikiPageKey wikiKey,
    Map<String, String> widgetDescriptor,
    Callback widgetRefreshRequired,
    Long wikiVersionInView
  ) {
    // parse referenced entities and put into start refs
    String height = DEFAULT_HEIGHT + "px";
    startRefs = new ArrayList<Reference>();
    String entityListStr = null;
    if (
      widgetDescriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY)
    ) entityListStr =
      widgetDescriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY);
    if (entityListStr != null) {
      String[] refs = entityListStr.split(
        WidgetConstants.PROV_WIDGET_ENTITY_LIST_DELIMETER
      );
      if (refs != null) {
        for (String refString : refs) {
          // Only add valid References
          Reference ref = DisplayUtils.parseEntityVersionString(refString);
          if (ref != null && ref.getTargetId() != null) {
            startRefs.add(ref);
          }
        }
      }
    }
    // backwards compatibility for original ProvenanceWidget API
    if (
      widgetDescriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY)
    ) {
      startRefs.add(
        DisplayUtils.parseEntityVersionString(
          widgetDescriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY)
        )
      );
    }
    if (
      widgetDescriptor.containsKey(
        WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY
      )
    ) {
      height =
        Integer.parseInt(
          widgetDescriptor.get(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY)
        ) +
        "px";
    }
    view.configure(startRefs, height);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public static Map<String, String> getDefaultWidgetDescriptor() {
    Map<String, String> configMap = new HashMap<String, String>();
    configMap.put(
      WidgetConstants.PROV_WIDGET_EXPAND_KEY,
      Boolean.toString(true)
    );
    configMap.put(
      WidgetConstants.PROV_WIDGET_UNDEFINED_KEY,
      Boolean.toString(true)
    );
    configMap.put(
      WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY,
      Integer.toString(DEFAULT_HEIGHT)
    );
    return configMap;
  }
}
