package org.sagebionetworks.web.client.widget.entity;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.ReactRef;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseGridHandle;
import org.sagebionetworks.web.client.jsinterop.SynapseGridProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SynapseGridImpl extends ReactComponent {

  private ReactRef<SynapseGridHandle> gridRef;
  private PortalGinInjector ginInjector;

  @Inject
  public SynapseGridImpl(PortalGinInjector ginInjector) {
    this.ginInjector = ginInjector;
  }

  public void configure(String query, Boolean showDebugInfo) {
    this.gridRef = React.createRef();
    SynapseGridProps props = SynapseGridProps.create(
      query,
      showDebugInfo,
      gridRef
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseGrid,
      props
    );
    this.render(component);
  }

  /**
   * Imperatively initialize the grid session via React ref
   */
  public void initializeGrid() {
    if (gridRef == null || gridRef.current == null) return;
    gridRef.current.initializeGrid();
  }
}
