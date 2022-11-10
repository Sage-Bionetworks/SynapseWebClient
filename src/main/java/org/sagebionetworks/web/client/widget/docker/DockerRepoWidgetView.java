package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerRepoWidgetView {
  void setProvenance(Widget widget);

  Widget asWidget();

  void setWikiPage(Widget widget);

  void setSynapseAlert(Widget widget);

  void setDockerPullCommand(String command);

  void setTitlebar(Widget widget);

  void setEntityMetadata(Widget widget);

  void setModifiedCreatedBy(IsWidget widget);

  void setDockerCommitListWidget(Widget widget);

  void setProvenanceWidgetVisible(boolean visible);
}
