package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.ShareThisPageProps.Callback;

public interface ShareThisPage extends IsWidget {
  void configure(
    String shortIoPublicApiKey,
    String domain,
    boolean open,
    Callback onClose,
    String renderAs
  );
}
