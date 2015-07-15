package org.sagebionetworks.web.client.widget.search;
import org.sagebionetworks.web.client.utils.CallbackP;

public interface SuggestionProvider {
	
	void getSuggestions(int offset, int pageSize, int width, String prefix,
			CallbackP<SynapseSuggestionBundle> callback);
	
}
