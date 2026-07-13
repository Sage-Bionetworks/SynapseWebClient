package org.sagebionetworks.web.client.widget.provenance.v2;

import com.google.gwt.core.client.JavaScriptObject;
import java.util.List;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.jsinterop.ProvenanceGraphProps;
import org.sagebionetworks.web.client.jsinterop.ProvenanceGraphProps.OnUpdateJavaScriptObject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ProvenanceWidgetViewImpl
  extends ReactComponent
  implements ProvenanceWidgetView {

  List<Reference> refs;
  String containerHeight;
  ProvenanceGraphProps.OnEditProvenance onEditProvenance;
  // this view stores the nodes and edges from this instance.  So you can reinitialize the previous state by calling rerender on this instance!
  JavaScriptObject initialNodes;
  JavaScriptObject initialEdges;
  OnUpdateJavaScriptObject nodesListener;
  OnUpdateJavaScriptObject edgesListener;

  @Inject
  public ProvenanceWidgetViewImpl() {
    addStyleName("overflowHidden");
    this.nodesListener =
      jsObject -> {
        initialNodes = jsObject;
      };
    this.edgesListener =
      jsObject -> {
        initialEdges = jsObject;
      };
  }

  public void configure(
    List<Reference> refs,
    String containerHeight,
    ProvenanceGraphProps.OnEditProvenance onEditProvenance
  ) {
    this.refs = refs;
    this.containerHeight = containerHeight;
    this.onEditProvenance = onEditProvenance;
    renderComponent();
  }

  public void renderComponent() {
    ProvenanceGraphProps props = ProvenanceGraphProps.create(
      refs,
      containerHeight,
      initialNodes,
      initialEdges,
      nodesListener,
      edgesListener,
      onEditProvenance
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ProvenanceGraph,
      props
    );
    this.render(component);
  }
}
