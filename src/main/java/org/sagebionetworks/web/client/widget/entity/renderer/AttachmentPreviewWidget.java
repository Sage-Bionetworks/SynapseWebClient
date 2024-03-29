package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class AttachmentPreviewWidget implements WidgetRendererPresenter {

  private AttachmentPreviewWidgetView view;
  private Map<String, String> descriptor;

  @Inject
  public AttachmentPreviewWidget(AttachmentPreviewWidgetView view) {
    this.view = view;
  }

  @Override
  public void configure(
    final WikiPageKey wikiKey,
    final Map<String, String> widgetDescriptor,
    Callback widgetRefreshRequired,
    Long wikiVersionInView
  ) {
    this.descriptor = widgetDescriptor;
    view.configure(
      wikiKey,
      descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY)
    );
    // set up view based on descriptor parameters
    descriptor = widgetDescriptor;
  }

  @SuppressWarnings("unchecked")
  public void clearState() {}

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
