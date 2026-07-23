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
      MetadataTasksPageProps.create(projectId, getRouterBaseName(projectId))
    );
  }

  /**
   * Updates the projectId and key props. Changing the key forces React to remount the component (rather than
   * reconcile it), which resets any internal state -- notably the internal router's current route.
   * <p>
   * Does not render; callers are responsible for calling {@link #render()} once after updating props.
   */
  public void setProjectIdAndKey(String projectId, String key) {
    props.projectId = projectId;
    props.routerBaseName = getRouterBaseName(projectId);
    props.key = key;
  }

  private static String getRouterBaseName(String projectId) {
    return "/Synapse:" + projectId + "/metadata/";
  }
}
