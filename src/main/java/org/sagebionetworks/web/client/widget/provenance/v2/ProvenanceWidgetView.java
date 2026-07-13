package org.sagebionetworks.web.client.widget.provenance.v2;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.jsinterop.ProvenanceGraphProps;

public interface ProvenanceWidgetView extends IsWidget {
  void configure(
    List<Reference> refs,
    String containerHeight,
    ProvenanceGraphProps.OnEditProvenance onEditProvenance
  );
}
