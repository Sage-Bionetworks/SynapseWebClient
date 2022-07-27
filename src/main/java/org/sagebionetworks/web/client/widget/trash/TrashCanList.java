package org.sagebionetworks.web.client.widget.trash;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class TrashCanList extends ReactComponentDiv {
	public TrashCanList(SynapseContextPropsProvider contextPropsProvider) {
		ReactDOM.render(
			React.createElementWithSynapseContext(
					SRC.SynapseComponents.TrashCanList,
					null,
					contextPropsProvider.getJsInteropContextProps()
			),
			getElement()
		);
	}
}
