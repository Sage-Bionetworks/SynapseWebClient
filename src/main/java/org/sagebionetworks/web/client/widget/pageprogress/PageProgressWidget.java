package org.sagebionetworks.web.client.widget.pageprogress;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.utils.Callback;

public class PageProgressWidget implements IsWidget {

  private PageProgressWidgetView view;

  @Inject
  public PageProgressWidget(PageProgressWidgetView view) {
    this.view = view;
  }

  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(
    String barColor,
    int barPercent,
    String backBtnLabel,
    Callback backBtnCallback,
    String forwardBtnLabel,
    Callback forwardBtnCallback,
    boolean isForwardActive
  ) {
    view.configure(
      barColor,
      barPercent,
      backBtnLabel,
      backBtnCallback,
      forwardBtnLabel,
      forwardBtnCallback,
      isForwardActive
    );
  }

  public void clear() {
    view.clear();
  }
}
