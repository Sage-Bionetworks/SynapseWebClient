package org.sagebionetworks.web.client.widget.pageprogress;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.utils.Callback;

public interface PageProgressWidgetView extends IsWidget {
  void clear();

  void setVisible(boolean visible);

  void configure(
    String barColor,
    int barPercent,
    String backBtnLabel,
    Callback backBtnCallback,
    String forwardBtnLabel,
    Callback forwardBtnCallback,
    boolean isForwardActive
  );
}
