package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Link;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.EntityBadgeIconsProps;
import org.sagebionetworks.web.client.jsinterop.SynapseClientError;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeView;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class EntityBadgeTest {

  private static final String USER_ID = "12430";

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  EntityBadgeView mockView;

  String entityId = "syn123";
  Long versionNumber = 5L;
  String entityName = "An Entity";
  EntityBadge widget;

  @Mock
  LazyLoadHelper mockLazyLoadHelper;

  @Mock
  PublicPrincipalIds mockPublicPrincipalIds;

  @Mock
  SynapseJavascriptClient mockSynapseJavascriptClient;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  SynapseProperties mockSynapseProperties;

  @Mock
  EventBus mockEventBus;

  @Mock
  S3FileHandle mockDataFileHandle;

  @Mock
  AuthenticationController mockAuthController;

  @Mock
  ClickHandler mockClickHandler;

  @Mock
  CookieProvider mockCookies;

  @Mock
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Captor
  ArgumentCaptor<EntityBadgeIconsProps> iconsPropsArgumentCaptor;

  @Captor
  ArgumentCaptor<EntityBundleRequest> entityBundleRequestCaptor;

  @Before
  public void before() throws JSONObjectAdapterException {
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn(null);
    widget =
      new EntityBadge(
        mockView,
        mockGlobalApplicationState,
        mockSynapseJavascriptClient,
        mockLazyLoadHelper,
        mockPopupUtils,
        mockEventBus,
        mockAuthController,
        propsProvider
      );

    when(mockAuthController.isLoggedIn()).thenReturn(true);
    when(mockSynapseProperties.getPublicPrincipalIds())
      .thenReturn(mockPublicPrincipalIds);
    when(mockView.isAttached()).thenReturn(true);
    AsyncMockStubber
      .callSuccessWith((Object) null)
      .when(mockSynapseJavascriptClient)
      .addFileToDownloadList(anyString(), anyString(), any());
    AsyncMockStubber
      .callSuccessWith((Object) null)
      .when(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(anyString(), anyLong(), any());
    when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(USER_ID);
  }

  private EntityBundle setupEntity(Entity entity) {
    EntityBundle bundle = mock(EntityBundle.class);
    when(bundle.getEntity()).thenReturn(entity);
    when(bundle.getFileHandles())
      .thenReturn(Collections.singletonList(mockDataFileHandle));
    AsyncMockStubber
      .callSuccessWith(bundle)
      .when(mockSynapseJavascriptClient)
      .getEntityBundle(anyString(), any(EntityBundleRequest.class), any());

    return bundle;
  }

  private EntityHeader configure() {
    EntityHeader header = new EntityHeader();
    header.setId(entityId);
    header.setName(entityName);
    header.setVersionNumber(versionNumber);
    widget.configure(header);
    return header;
  }

  @Test
  public void testConfigure() throws Exception {
    EntityHeader header = configure();

    verify(mockView).clearEntityInformation();
    verify(mockView).setEntity(header);
  }

  /**
   * This tests the standard case when the badge is outside the viewport and scrolled into view.
   */
  @Test
  public void testCheckForInViewAndLoadData() {
    // set up entity
    when(mockPublicPrincipalIds.isPublic(anyLong())).thenReturn(true);
    String entityId = "syn12345";
    Project testProject = new Project();
    testProject.setId(entityId);
    setupEntity(testProject);

    // configure
    configure();

    ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
    verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
    captor.getValue().invoke();

    verify(mockSynapseJavascriptClient)
      .getEntityBundle(
        anyString(),
        entityBundleRequestCaptor.capture(),
        any(AsyncCallback.class)
      );
    verify(mockView).setIcons(any(), any());
    verify(mockView, never()).showAddToDownloadList();
    EntityBundleRequest request = entityBundleRequestCaptor.getValue();
    assertTrue(request.getIncludeEntity());
    assertTrue(request.getIncludeFileHandles());
    assertNull(request.getIncludeAnnotations());
    assertNull(request.getIncludeBenefactorACL());
  }

  @Test
  public void testGetFileEntityBundle() {
    // verify download button is configured and shown
    String entityId = "syn12345";
    FileEntity testFile = new FileEntity();
    testFile.setId(entityId);
    testFile.setDataFileHandleId("123");
    when(mockPublicPrincipalIds.isPublic(anyLong())).thenReturn(true);
    setupEntity(testFile);

    configure();
    widget.getEntityBundle();

    verify(mockSynapseJavascriptClient)
      .getEntityBundle(
        anyString(),
        any(EntityBundleRequest.class),
        any(AsyncCallback.class)
      );
    verify(mockView).clearIcons();
    verify(mockView).setIcons(any(), any());
    verify(mockView).showAddToDownloadList();
  }

  @Test
  public void testCheckForInViewAndLoadDataFailure() {
    configure();
    // test failure response from getEntityBundle
    String errorMessage = "problem occurred while asking for entity bundle";
    Exception ex = new Exception(errorMessage);
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockSynapseJavascriptClient)
      .getEntityBundle(
        anyString(),
        any(EntityBundleRequest.class),
        any(AsyncCallback.class)
      );
    widget.getEntityBundle();

    verify(mockView).setError(errorMessage);
  }

  @Test
  public void testEntityClickedCustomHandler() {
    widget.setClickHandler(mockClickHandler);
    // verify click handler is set when the view is configured
    verify(mockView, never()).setClickHandler(any(ClickHandler.class));

    configure();

    verify(mockView).setClickHandler(mockClickHandler);
  }

  @Test
  public void testGetEntity() {
    EntityHeader header = new EntityHeader();
    header.setId("syn12345");
    widget.configure(header);
    assertSame(header, widget.getHeader());
  }

  @Test
  public void testOnUnlink() {
    configure();
    EntityBundle bundle = setupEntity(new Link());
    widget.setEntityBundle(bundle);

    // Simulate successful delete of Link entity by invoking the prop passed to the React component
    verify(mockView).setIcons(iconsPropsArgumentCaptor.capture(), any());
    iconsPropsArgumentCaptor
      .getValue()
      .getOnUnlinkSuccess()
      .onUnlinkSuccess(bundle.getEntity().getId());

    verify(mockPopupUtils).showInfo(EntityBadge.LINK_SUCCESSFULLY_DELETED);
  }

  @Test
  public void testOnUnlinkFailure() {
    // simulate failure to delete of Link entity
    String errorMessage = "error occurred";
    SynapseClientError clientError = new SynapseClientError();
    clientError.setReason(errorMessage);

    configure();
    EntityBundle bundle = setupEntity(new Link());
    widget.setEntityBundle(bundle);

    // Simulate the error by invoking the prop passed to the React component
    verify(mockView).setIcons(iconsPropsArgumentCaptor.capture(), any());
    iconsPropsArgumentCaptor
      .getValue()
      .getOnUnlinkError()
      .onUnlinkError(clientError);

    verify(mockPopupUtils).showErrorMessage(errorMessage);
  }

  @Test
  public void testContentSize() {
    String friendlySize = "44MB";
    when(mockView.getFriendlySize(anyLong(), anyBoolean()))
      .thenReturn(friendlySize);
    String result = widget.getContentSize(null);
    assertEquals("", result);

    FileHandle s3FileHandle = new S3FileHandle();
    s3FileHandle.setContentSize(500L);

    result = widget.getContentSize(s3FileHandle);
    assertEquals(friendlySize, result);
  }

  @Test
  public void testContentMd5() {
    String result = widget.getContentMd5(null);
    assertEquals("", result);

    FileHandle s3FileHandle = new S3FileHandle();
    String contentMd5 = "fghij";
    s3FileHandle.setContentMd5(contentMd5);

    result = widget.getContentMd5(s3FileHandle);
    assertEquals(contentMd5, result);
  }

  @Test
  public void testOnAddToDownloadListV2() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    FileEntity testFile = new FileEntity();
    testFile.setId(entityId);
    testFile.setVersionNumber(versionNumber);
    setupEntity(testFile);
    EntityHeader header = configure();

    widget.getEntityBundle();

    widget.onAddToDownloadList();

    verify(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(
        eq(header.getId()),
        eq(header.getVersionNumber()),
        any()
      );
    verify(mockPopupUtils)
      .showInfo(
        header.getName() + EntityBadge.ADDED_TO_DOWNLOAD_LIST,
        "#!DownloadCart:0",
        DisplayConstants.VIEW_DOWNLOAD_LIST
      );
    verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
  }

  @Test
  public void testOnAddToDownloadListV2Error() {
    when(
      mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
    )
      .thenReturn("true");
    String errorMessage = "a simulated error";
    AsyncMockStubber
      .callFailureWith(new Exception(errorMessage))
      .when(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(anyString(), anyLong(), any());

    FileEntity testFile = new FileEntity();
    testFile.setId(entityId);
    testFile.setVersionNumber(versionNumber);
    setupEntity(testFile);
    EntityHeader header = configure();

    widget.getEntityBundle();

    widget.onAddToDownloadList();

    verify(mockSynapseJavascriptClient)
      .addFileToDownloadListV2(
        eq(header.getId()),
        eq(header.getVersionNumber()),
        any()
      );
    verifyZeroInteractions(mockPopupUtils, mockEventBus);
    verify(mockView).setError(errorMessage);
  }
}
