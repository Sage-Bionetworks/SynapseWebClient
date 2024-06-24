package org.sagebionetworks.web.client.widget.certificationquiz;

import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class CertificationQuiz extends ReactComponent {

  public CertificationQuiz(
    SynapseReactClientFullContextPropsProvider contextPropsProvider
  ) {
    this.render(
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.CertificationQuiz,
          null,
          contextPropsProvider.getJsInteropContextProps()
        )
      );
  }
}
