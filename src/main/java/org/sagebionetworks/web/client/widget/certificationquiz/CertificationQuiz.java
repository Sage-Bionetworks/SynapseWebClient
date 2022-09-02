package org.sagebionetworks.web.client.widget.certificationquiz;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class CertificationQuiz extends ReactComponentDiv {
    public CertificationQuiz(SynapseContextPropsProvider contextPropsProvider){
        ReactDOM.render(
                React.createElementWithSynapseContext(
                        SRC.SynapseComponents.CertificationQuiz,
                        null,
                        contextPropsProvider.getJsInteropContextProps()
                ),
                getElement()
        );
    }
}
