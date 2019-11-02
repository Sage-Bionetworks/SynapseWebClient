package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Mockito.verify;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.widget.entity.editor.LeaderboardConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;

public class LeaderboardConfigEditorTest {

	LeaderboardConfigEditor editor;
	@Mock
	QueryTableConfigEditor mockQueryTableEditor;

	@Test
	public void testSetServicePrefix() {
		MockitoAnnotations.initMocks(this);
		editor = new LeaderboardConfigEditor(mockQueryTableEditor);
		verify(mockQueryTableEditor).setServicePrefix(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX);
		verify(mockQueryTableEditor).setQueryPlaceholder(LeaderboardConfigEditor.LEADERBOARD_QUERY_PLACEHOLDER);
	}
}
