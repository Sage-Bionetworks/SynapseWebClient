package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface ReactFunctionComponent<P extends ReactComponentProps> {
	ReactElement run(P props);
}
