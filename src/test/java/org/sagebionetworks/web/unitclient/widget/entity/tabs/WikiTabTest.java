package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.shared.WikiPageKey;

@RunWith(MockitoJUnitRunner.class)
public class WikiTabTest {

  @Mock
  Tab mockTab;

  @Mock
  WikiPageWidget mockWikiPageWidget;

  @Mock
  CallbackP<Tab> mockOnClickCallback;

  @Captor
  ArgumentCaptor<CallbackP<String>> mockWikiReloadHandlerCaptor;

  @Mock
  PortalGinInjector mockPortalGinInjector;

  @Mock
  EntityActionMenu mockActionMenuWidget;

  @Mock
  AddToDownloadListV2 mockAddToDownloadListWidget;

  @Mock
  EntityBundle mockProjectEntityBundle;

  @Mock
  SynapseJavascriptClient mockJsClient;

  WikiTab tab;

  @Before
  public void setUp() {
    tab = new WikiTab(mockTab, mockPortalGinInjector);

    when(mockTab.getEntityActionMenu()).thenReturn(mockActionMenuWidget);
    when(mockPortalGinInjector.getWikiPageWidget())
      .thenReturn(mockWikiPageWidget);
    when(mockPortalGinInjector.getSynapseJavascriptClient())
      .thenReturn(mockJsClient);
    when(mockPortalGinInjector.getAddToDownloadListV2())
      .thenReturn(mockAddToDownloadListWidget);
    tab.lazyInject();
  }

  @Test
  public void testSetTabClickedCallback() {
    tab.setTabClickedCallback(mockOnClickCallback);
    verify(mockTab).addTabClickedCallback(mockOnClickCallback);
  }

  @Test
  public void testConfigure() {
    String entityId = "syn1";
    String entityName = "mr. bean";
    String wikiPageId = "9";
    Boolean canEdit = true;
    WikiPageWidget.Callback callback = mock(WikiPageWidget.Callback.class);
    tab.configure(
      entityId,
      entityName,
      mockProjectEntityBundle,
      wikiPageId,
      canEdit,
      callback
    );

    verify(mockWikiPageWidget)
      .configure(any(WikiPageKey.class), eq(canEdit), eq(callback));
    verify(mockWikiPageWidget).showSubpages(mockActionMenuWidget);
    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockTab).setEntityNameAndPlace(eq(entityName), captor.capture());
    Synapse place = captor.getValue();
    assertEquals(entityId, place.getEntityId());
    assertNull(place.getVersionNumber());
    assertEquals(EntityArea.WIKI, place.getArea());
    assertEquals(wikiPageId, place.getAreaToken());
  }

  @Test
  public void testSetWikiReloadHandler() {
    String entityId = "syn1";
    String entityName = "project 1";
    String wikiPageId = "9";
    Boolean canEdit = true;
    WikiPageWidget.Callback callback = mock(WikiPageWidget.Callback.class);
    tab.configure(
      entityId,
      entityName,
      mockProjectEntityBundle,
      wikiPageId,
      canEdit,
      callback
    );

    verify(mockWikiPageWidget)
      .setWikiReloadHandler(mockWikiReloadHandlerCaptor.capture());
    CallbackP<String> mockWikiReloadHandler = mockWikiReloadHandlerCaptor.getValue();

    // simulate reload of a different wiki page ID
    String newSubWikiPageId = "10";
    mockWikiReloadHandler.invoke(newSubWikiPageId);

    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockTab, times(2))
      .setEntityNameAndPlace(eq(entityName), captor.capture());
    Synapse place = captor.getValue();
    assertEquals(entityId, place.getEntityId());
    assertNull(place.getVersionNumber());
    assertEquals(EntityArea.WIKI, place.getArea());
    assertEquals(newSubWikiPageId, place.getAreaToken());

    // SWC-5078: action controller should have also been reconfigured when a subpage was clicked
    verify(mockTab)
      .configureEntityActionController(
        mockProjectEntityBundle,
        true,
        newSubWikiPageId,
        null
      );
  }

  @Test
  public void testAsTab() {
    assertEquals(mockTab, tab.asTab());
  }
}
