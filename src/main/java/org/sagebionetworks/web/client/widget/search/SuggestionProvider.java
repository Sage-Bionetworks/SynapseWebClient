package org.sagebionetworks.web.client.widget.search;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SuggestionProvider {
	
	void getSuggestions(int offset, int pageSize, int width, String prefix,
			AsyncCallback<SynapseSuggestionBundle> asyncCallback);
	
}
