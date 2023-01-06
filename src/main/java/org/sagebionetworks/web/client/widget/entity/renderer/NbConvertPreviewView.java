package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface NbConvertPreviewView extends IsWidget {
  void setHtml(String html);

  void setRawHtml(String rawHtml);

  void setSynAlert(IsWidget w);

  void setLoadingVisible(boolean visible);

  void openRawHtmlInNewWindow();

  void openInNewWindow(String url);

  void setPresenter(Presenter p);

  public interface Presenter {
    void onShowFullContent();
  }
}
