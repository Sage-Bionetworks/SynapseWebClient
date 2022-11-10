package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Date;

public interface DockerCommitRowWidgetView extends IsWidget {
  public interface Presenter {
    void onClick();
  }

  void setPresenter(Presenter dockerCommitRowWidget);

  void setTag(String tag);

  void setCreatedOn(Date date);

  void setDigest(String widget);
}
