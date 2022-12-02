package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TabView;

public class TabTest {

  @Mock
  TabView mockView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  SynapseJSNIUtils mockSynapseJSNIUtils;

  @Mock
  GWTWrapper mockGWT;

  @Mock
  EntityActionController mockActionController;

  @Mock
  EntityActionMenu mockActionMenu;

  @Mock
  AddToDownloadListV2 mockAddToDownloadListWidget;

  @Mock
  EntityBundle mockEntityBundle;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Mock
  CallbackP<Tab> mockOnClickCallback;

  Tab tab;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    tab =
      new Tab(
        mockView,
        mockGlobalAppState,
        mockSynapseJSNIUtils,
        mockGWT,
        mockActionController,
        mockActionMenu,
        mockPopupUtils
      );
    when(mockGlobalAppState.isEditing()).thenReturn(false);
    when(mockView.isActive()).thenReturn(true);
  }

  private void initPlace() {
    String entityName = "one project to rule them all";
    String entityId = "syn123";
    Synapse place = new Synapse(entityId);
    tab.setEntityNameAndPlace(entityName, place);
  }

  @Test
  public void testConfigure() {
    // test construction
    verify(mockView).setPresenter(tab);
    // and configure
    String tabTitle = "TestTab";
    Widget content = null;
    tab.configure(tabTitle, "file", "help markdown", "link", EntityArea.FILES);
    verify(mockView)
      .configure(eq(tabTitle), eq("file"), anyString(), anyString());
    tab.setContent(content);
    verify(mockView).setContent(content);
  }

  @Test
  public void testConfigureEntityActionController() {
    EntityArea area = EntityArea.FILES;
    boolean isCurrentVersion = false;
    String wikiPageKey = null;

    tab.configure("Files", "file", "help markdown", "link", EntityArea.FILES);
    tab.configureEntityActionController(
      mockEntityBundle,
      isCurrentVersion,
      wikiPageKey,
      mockAddToDownloadListWidget
    );

    verify(mockActionController)
      .configure(
        mockActionMenu,
        mockEntityBundle,
        isCurrentVersion,
        wikiPageKey,
        area,
        mockAddToDownloadListWidget
      );
  }

  @Test
  public void testSetEntityNameAndPlace() {
    // verify page title is set during this process
    // note: tab view is configured to reply that tab is active
    String entityName = "one project to rule them all";
    String entityId = "syn123";
    Synapse place = new Synapse(entityId);
    tab.configure("Files", "file", "help markdown", "link", EntityArea.FILES);
    tab.setEntityNameAndPlace(entityName, place);
    verify(mockSynapseJSNIUtils)
      .setPageTitle(entityName + " - " + entityId + " - Files");
  }

  @Test
  public void testShowTab() {
    initPlace();
    tab.setEntityName("entity name");
    tab.showTab();
    verify(mockGlobalAppState).pushCurrentPlace(any(Place.class));
    verify(mockView).setActive(true);
    // verify showing tab also attempts to update the page title
    verify(mockSynapseJSNIUtils, atLeastOnce()).setPageTitle(anyString());
  }

  @Test
  public void testShowTabReplaceState() {
    initPlace();
    tab.setEntityName("entity name");
    boolean pushState = false;
    tab.showTab(pushState);
    verify(mockGlobalAppState).replaceCurrentPlace(any(Place.class));
    verify(mockView).setActive(true);
    // verify showing tab also attempts to update the page title
    verify(mockSynapseJSNIUtils, atLeastOnce()).setPageTitle(anyString());
  }

  @Test
  public void testShowTabPushState() {
    initPlace();
    boolean pushState = true;
    tab.showTab(pushState);
    verify(mockGlobalAppState).pushCurrentPlace(any(Place.class));
    verify(mockView).setActive(true);
    // verify showing tab also attempts to update the page title
    verify(mockSynapseJSNIUtils, atLeastOnce()).setPageTitle(anyString());
  }

  @Test
  public void testSetEntityNameAndPlaceNotActive() {
    when(mockView.isActive()).thenReturn(false);
    // verify page title is not set during this process (if tab is not active)
    String entityName = "one project to rule them all";
    String entityId = "syn123";
    Synapse place = new Synapse(entityId);
    tab.setEntityNameAndPlace(entityName, place);
    verify(mockSynapseJSNIUtils, never()).setPageTitle(anyString());
  }

  @Test
  public void testShowTabWithoutPlace() {
    tab.showTab();
    verify(mockGlobalAppState, never()).pushCurrentPlace(any(Place.class));
    verify(mockView, never()).setActive(true);
    verify(mockGWT).scheduleExecution(callbackCaptor.capture(), anyInt());

    // after init, if the callback is invoked then the place is pushed
    initPlace();
    callbackCaptor.getValue().invoke();
    verify(mockGlobalAppState).pushCurrentPlace(any(Place.class));
    verify(mockView).setActive(true);
  }

  @Test
  public void testAddTabListItemStyle() {
    String style = "min-width-150";
    tab.addTabListItemStyle(style);
    verify(mockView).addTabListItemStyle(style);
  }

  @Test
  public void testOnTabClicked() {
    tab.configure("TestTab", "file", "help markdown", "link", EntityArea.FILES);
    tab.addTabClickedCallback(mockOnClickCallback);

    tab.onTabClicked();

    verify(mockOnClickCallback).invoke(tab);
  }

  @Test
  public void testOnTabClickedWhileEditing() {
    when(mockGlobalAppState.isEditing()).thenReturn(true);
    tab.configure("TestTab", "file", "help markdown", "link", EntityArea.FILES);
    tab.addTabClickedCallback(mockOnClickCallback);

    tab.onTabClicked();

    verify(mockPopupUtils)
      .showConfirmDialog(
        anyString(),
        eq(DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE),
        callbackCaptor.capture()
      );
    verify(mockOnClickCallback, never()).invoke(tab);

    //simulate confirm
    callbackCaptor.getValue().invoke();

    verify(mockOnClickCallback).invoke(tab);
  }
}
