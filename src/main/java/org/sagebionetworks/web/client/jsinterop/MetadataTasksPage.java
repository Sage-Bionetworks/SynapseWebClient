package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.web.client.widget.ReactComponentV2;

public class MetadataTasksPage
  extends ReactComponentV2<
    ReactComponentType<MetadataTasksPageProps>,
    MetadataTasksPageProps
  > {

  public MetadataTasksPage(String projectId) {
    super(
      SRC.SynapseComponents.MetadataTasksPage,
      MetadataTasksPageProps.create(
        projectId,
        "/Synapse:" + projectId + "/metadata/"
      )
    );
  }

  public void setProjectId(String projectId) {
    props.projectId = projectId;
    this.render();
  }
}
