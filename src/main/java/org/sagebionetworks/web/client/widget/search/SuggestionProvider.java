package org.sagebionetworks.web.client.widget.search;
import java.util.List;

import org.sagebionetworks.web.client.utils.CallbackP;

public interface SuggestionProvider {

	void configure(String width, String baseFileHandleUrl,
			String baseProfileAttachmentUrl);

	void getSuggestions(int offset, int pageSize, String query,
			CallbackP<List<SynapseSuggestion>> callbackP);
}
