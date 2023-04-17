package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;

public interface TrashView extends IsWidget {
  void createReactComponentWidget(
    SynapseReactClientFullContextPropsProvider propsProvider
  );
}
