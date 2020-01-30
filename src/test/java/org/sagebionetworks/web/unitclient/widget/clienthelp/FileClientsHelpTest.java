package org.sagebionetworks.web.unitclient.widget.clienthelp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelpView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class FileClientsHelpTest {
	FileClientsHelp widget;
	@Mock
	FileClientsHelpView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	List<VersionInfo> versions;
	public static final String ENTITY_ID = "syn29382";
	public static final Long ENTITY_VERSION = 42L;

	@Before
	public void setUp() throws Exception {
		versions = new ArrayList<VersionInfo>();
		AsyncMockStubber.callSuccessWith(versions).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget = new FileClientsHelp(mockView, mockJsClient, mockJsniUtils);
	}

	@Test
	public void testConfigureNoVersions() {
		widget.configureAndShow(ENTITY_ID, ENTITY_VERSION);

		verify(mockView).configureAndShow(ENTITY_ID, ENTITY_VERSION);
		verify(mockJsClient).getEntityVersions(eq(ENTITY_ID), eq(0), eq(1), any(AsyncCallback.class));
		verify(mockView).setVersionVisible(false);
	}

	@Test
	public void testConfigureCurrentVersion() {
		VersionInfo currentVersion = new VersionInfo();
		currentVersion.setVersionNumber(ENTITY_VERSION);
		versions.add(currentVersion);

		widget.configureAndShow(ENTITY_ID, ENTITY_VERSION);

		verify(mockView).configureAndShow(ENTITY_ID, ENTITY_VERSION);
		verify(mockJsClient).getEntityVersions(eq(ENTITY_ID), eq(0), eq(1), any(AsyncCallback.class));
		// showing current version, so show instructions on how to get the latest version
		verify(mockView).setVersionVisible(false);
	}

	@Test
	public void testConfigureOldVersion() {
		VersionInfo currentVersion = new VersionInfo();
		currentVersion.setVersionNumber(100L);
		versions.add(currentVersion);

		widget.configureAndShow(ENTITY_ID, ENTITY_VERSION);

		verify(mockView).configureAndShow(ENTITY_ID, ENTITY_VERSION);
		verify(mockJsClient).getEntityVersions(eq(ENTITY_ID), eq(0), eq(1), any(AsyncCallback.class));
		// not showing current version, so show instructions on how to get the version that's being
		// displayed
		verify(mockView).setVersionVisible(true);
	}

	@Test
	public void testConfigureFailureToGetCurrentVersion() {
		String errorMessage = "could not retrieve";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockJsClient).getEntityVersions(anyString(), anyInt(), anyInt(), any(AsyncCallback.class));

		widget.configureAndShow(ENTITY_ID, ENTITY_VERSION);

		verify(mockView, never()).configureAndShow(anyString(), anyLong());
		verify(mockJsClient).getEntityVersions(eq(ENTITY_ID), eq(0), eq(1), any(AsyncCallback.class));
		verify(mockView).setVersionVisible(false);
		verify(mockJsniUtils).consoleError(errorMessage);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
