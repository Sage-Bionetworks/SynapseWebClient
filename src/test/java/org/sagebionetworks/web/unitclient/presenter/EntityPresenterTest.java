package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EntityPresenterTest {
	
	EntityPresenter entityPresenter;
	EntityView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	CookieProvider mockCookies;
	PlaceChanger mockPlaceChanger;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	SynapseAlert mockSynAlert;
	OpenTeamInvitationsWidget mockOpenInviteWidget;
	Header mockHeaderWidget;
	EntityPageTop mockEntityPageTop;
	Footer mockFooterWidget;
	String EntityId = "1";
	Synapse place = new Synapse("Synapse:"+ EntityId);
	Entity EntityModel1;
	EntityBundle eb;
	String entityId = "syn43344";
	Synapse.EntityArea area = Synapse.EntityArea.FILES;
	String areaToken = null;
	long id;
	
	String rootWikiId = "12333";
	FileHandleResults rootWikiAttachments;
	@Before
	public void setup() throws Exception{
		mockView = mock(EntityView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockCookies = mock(CookieProvider.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockOpenInviteWidget = mock(OpenTeamInvitationsWidget.class);
		mockHeaderWidget = mock(Header.class);
		mockEntityPageTop = mock(EntityPageTop.class);
		mockFooterWidget = mock(Footer.class);
		entityPresenter = new EntityPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient,
				mockCookies, mockSynapseJSNIUtils, mockSynAlert, mockEntityPageTop, mockHeaderWidget, mockFooterWidget, mockOpenInviteWidget);
		Entity testEntity = new Project();
		eb = new EntityBundle();
		eb.setEntity(testEntity);
		EntityPath path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		id=0L;
		AsyncMockStubber.callSuccessWith(rootWikiId).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		rootWikiAttachments = new FileHandleResults();
		AsyncMockStubber.callSuccessWith(rootWikiAttachments).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		when(mockGlobalApplicationState.isWikiBasedEntity(anyString())).thenReturn(false);
		
		verify(mockView).setEntityPageTopWidget(mockEntityPageTop);
		verify(mockView).setFooterWidget(mockFooterWidget);
		verify(mockView).setHeaderWidget(mockHeaderWidget);
		verify(mockView).setOpenTeamInvitesWidget(mockOpenInviteWidget);
		verify(mockEntityPageTop).setEntityUpdatedHandler(any(EntityUpdatedHandler.class));
		verify(mockEntityPageTop).setAreaChangeHandler(any(AreaChangeHandler.class));
		verify(mockHeaderWidget, never()).configure(false); // waits to configure for entity header
		verify(mockHeaderWidget).refresh();
	}	
	
	@Test
	public void testSetPlaceAndRefreshWithVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(1L);
		when(place.getEntityId()).thenReturn(entityId);
		
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockView).setBackgroundImageVisible(false);
		verify(mockSynapseClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockGlobalApplicationState).isWikiBasedEntity(entityId);
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop).clearState();
		verify(mockEntityPageTop).configure(eq(eb), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		verify(mockEntityPageTop).refresh();
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
		verify(mockHeaderWidget).configure(eq(false), any(EntityHeader.class));
	}
	
	@Test
	public void testSetPlaceAndRefreshWithoutVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockView).setBackgroundImageVisible(false);
		verify(mockSynapseClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockGlobalApplicationState).isWikiBasedEntity(entityId);
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop).clearState();
		verify(mockEntityPageTop).configure(eq(eb), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		verify(mockEntityPageTop).refresh();
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
	}
	
	@Test
	public void testStart() {
		entityPresenter.setPlace(place);		
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		entityPresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);		
	}
	
	@Test
	public void testSetPlaceAndRefreshWikiBasedEntity() {
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		//redirects to the wiki place
		verify(mockPlaceChanger).goTo(any(Wiki.class));
		verify(mockView, never()).setEntityPageTopVisible(true);
	}
	
	@Test
	public void testSetPlaceAndRefreshWikiBasedEntityInTestWebsite() {
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		Exception caught = new Exception("test");
		//will show full project page for wiki based entities when in alpha mode
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		entityPresenter.setPlace(place);
		ArgumentCaptor<AsyncCallback> captor = ArgumentCaptor.forClass(AsyncCallback.class);
		//verify synapse client call
		verify(mockSynAlert).handleException(caught);
	}
	
	private AccessRequirement createNewAR(ACCESS_TYPE type) {
		AccessRequirement ar = new TermsOfUseAccessRequirement();
		ar.setAccessType(type);
		id++;
		ar.setId(id);
		return ar;
	}
	
	@Test
	public void testSetPlaceAndRefreshFailure() {
		Exception caught = new Exception("test");
		//will show full project page for wiki based entities when in alpha mode
		AsyncMockStubber.callFailureWith(caught).when(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		when(mockGlobalApplicationState.isWikiBasedEntity(entityId)).thenReturn(true);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(caught);
		//header should be reconfigured to set back to Synapse
		verify(mockHeaderWidget).configure(false);
	}
	
	@Test
	public void testClear() {
		entityPresenter.clear();
		verify(mockView, times(2)).clear();
		verify(mockSynAlert, times(2)).clear();
		verify(mockOpenInviteWidget, times(2)).clear();
	}
	
	@Test
	public void testFilterToDownloadARs() {
		List<AccessRequirement> unfilteredARs = new ArrayList<AccessRequirement>();
		List<AccessRequirement> unfilteredUnmetARs = new ArrayList<AccessRequirement>();
		List<AccessRequirement> expectedFilteredARs = new ArrayList<AccessRequirement>();
		List<AccessRequirement> expectedFilteredUnmetARs = new ArrayList<AccessRequirement>();
		
		unfilteredARs.add(createNewAR(ACCESS_TYPE.UPLOAD));
		unfilteredUnmetARs.add(createNewAR(ACCESS_TYPE.UPLOAD));
		
		AccessRequirement ar = createNewAR(ACCESS_TYPE.DOWNLOAD);
		unfilteredARs.add(ar);
		expectedFilteredARs.add(ar);
		
		ar = createNewAR(ACCESS_TYPE.DOWNLOAD);
		unfilteredUnmetARs.add(ar);
		expectedFilteredUnmetARs.add(ar);
		
		unfilteredARs.add(createNewAR(ACCESS_TYPE.SUBMIT));
		unfilteredUnmetARs.add(createNewAR(ACCESS_TYPE.SUBMIT));
		
		eb.setAccessRequirements(unfilteredARs);
		eb.setUnmetAccessRequirements(unfilteredUnmetARs);
		EntityPresenter.filterToDownloadARs(eb);
		
		assertEquals(expectedFilteredARs, eb.getAccessRequirements());
		assertEquals(expectedFilteredUnmetARs, eb.getUnmetAccessRequirements());
	}
	
	@Test
	public void testLoadBackgroundImage() {
		String projectEntityId = "4";
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle backgroundImageFile = mock(FileHandle.class);
		when(backgroundImageFile.getFileName()).thenReturn(EntityPresenter.ENTITY_BACKGROUND_IMAGE_NAME);
		fileHandles.add(backgroundImageFile);
		rootWikiAttachments.setList(fileHandles);
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		//and it should have found the background image and decided to show it!
		verify(mockView).setBackgroundImageVisible(true);
		verify(mockView).setBackgroundImageUrl(anyString());
	}
	
	@Test
	public void testLoadBackgroundImageNoMatch() {
		String projectEntityId = "4";
		List<FileHandle> fileHandles = new ArrayList<FileHandle>();
		FileHandle backgroundImageFile = mock(FileHandle.class);
		when(backgroundImageFile.getFileName()).thenReturn("wrong file name.png");
		fileHandles.add(backgroundImageFile);
		rootWikiAttachments.setList(fileHandles);
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		//mismatch file name, should not show a background image
		verify(mockView, never()).setBackgroundImageVisible(true);
		verify(mockView, never()).setBackgroundImageUrl(anyString());
	}
	
	@Test
	public void testLoadBackgroundImageNoRootWiki() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		
		String projectEntityId = "4";
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient, never()).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		
		//no root wiki
		verify(mockView, never()).setBackgroundImageVisible(true);
		verify(mockView, never()).setBackgroundImageUrl(anyString());
	}
	
	@Test
	public void testLoadBackgroundImageNoAttachments() {
		String projectEntityId = "4";
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		//no attachments
		verify(mockView, never()).setBackgroundImageVisible(true);
		verify(mockView, never()).setBackgroundImageUrl(anyString());
	}
	
	@Test
	public void testLoadBackgroundImageWikiIdFailure() {
		String projectEntityId = "4";
		String exceptionMessage= "my test error message";
		AsyncMockStubber.callFailureWith(new Exception(exceptionMessage)).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockSynapseJSNIUtils).consoleError(exceptionMessage);
	}
	
	@Test
	public void testLoadBackgroundImageAttachmentListFailure() {
		String projectEntityId = "4";
		String exceptionMessage= "my test error message while getting wiki attachments";
		AsyncMockStubber.callFailureWith(new Exception(exceptionMessage)).when(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		entityPresenter.loadBackgroundImage(projectEntityId);
		verify(mockSynapseClient).getWikiAttachmentHandles(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockSynapseJSNIUtils).consoleError(exceptionMessage);
	}
	
	@Test
	public void testShow403() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		entityPresenter.setPlace(place);
		entityPresenter.show403();
		verify(mockSynAlert).show403(anyString());
		verify(mockView).setEntityPageTopVisible(false);
		verify(mockView).setOpenTeamInvitesVisible(true);
	}
}
