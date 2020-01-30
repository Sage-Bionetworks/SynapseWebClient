package org.sagebionetworks.web.unitclient.widget.breadcrumb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.BreadcrumbView;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;

public class BreadcrumbTest {

	Breadcrumb breadcrumb;
	BreadcrumbView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	SynapseClientAsync mockSynapseClient;
	IconsImageBundle mockIconsImageBundle;

	@Before
	public void setup() throws UnsupportedEncodingException, JSONObjectAdapterException {
		mockView = Mockito.mock(BreadcrumbView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);

		breadcrumb = new Breadcrumb(mockView, mockGlobalApplicationState);


		verify(mockView).setPresenter(breadcrumb);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAsWidgetEntity() throws Exception {
		Entity entity = new Folder();
		entity.setId("3");
		List<EntityHeader> pathHeaders = new ArrayList<EntityHeader>();

		EntityHeader rootHeader = new EntityHeader();
		rootHeader.setId("1");
		rootHeader.setName("root");
		pathHeaders.add(rootHeader);

		EntityHeader projHeader = new EntityHeader();
		projHeader.setId("2");
		projHeader.setName("project");
		pathHeaders.add(projHeader);

		EntityHeader dsHeader = new EntityHeader();
		dsHeader.setId("3");
		dsHeader.setName("ds");
		pathHeaders.add(dsHeader);

		EntityPath entityPath = new EntityPath();
		entityPath.setPath(pathHeaders);
		JSONObjectAdapter pathAdapter = new JSONObjectAdapterImpl();
		entityPath.writeToJSONObject(pathAdapter);

		breadcrumb.configure(entityPath, EntityArea.FILES);
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(mockView).setLinksList(captor.capture(), eq("ds"));
		List<LinkData> links = captor.getValue();
		assertNotNull(links);
		assertEquals(links.size(), 1);
	}


	@Test
	public void testAsWidgetLinksList() {
		// verify that setting the breadcrumb sets the view's links
		reset(mockView);
		List<LinkData> links = new ArrayList<LinkData>();
		LinkData homeLink = new LinkData("MyHomeLink", new Home(ClientProperties.DEFAULT_PLACE_TOKEN));
		links.add(homeLink);
		String currentPageName = "CurrentPage";
		breadcrumb.configure(links, currentPageName);
		verify(mockView).setLinksList(links, currentPageName);
	}


	@Test
	public void testAsWidget() {
		assertNull(breadcrumb.asWidget());
	}

}
