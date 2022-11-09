package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.docker.DockerCommit;

public interface DockerCommitListWidgetView extends IsWidget {
  public interface Presenter {
    DockerCommit getCurrentCommit();
  }

  void setCommitsContainer(IsWidget widget);

  void setPresenter(Presenter presenter);

  void setSynAlert(Widget widget);
}
