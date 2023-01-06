package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.jsinterop.EntityPageTitleBarProps;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuPropsJsInterop;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBarView;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuProps;

@RunWith(GwtMockitoTestRunner.class)
public class BasicTitleBarTest {

  BasicTitleBar titleBar;

  @Mock
  BasicTitleBarView mockView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  EntityActionMenu mockActionMenu;

  @Mock
  Widget mockActionMenuWidget;

  @Mock
  EntityBundle mockBundle;

  @Mock
  EntityActionMenuProps mockActionMenuProps;

  @Mock
  EntityActionMenuPropsJsInterop mockActionMenuJsInteropProps;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Captor
  ArgumentCaptor<EntityPageTitleBarProps> propsCaptor;

  @Captor
  ArgumentCaptor<Consumer<EntityActionMenuProps>> onActionMenuPropsChangeCaptor;

  @Captor
  ArgumentCaptor<AccessRequirementsPlace> placeCaptor;

  Folder folder;
  String testEntityName = "Entity Name";
  Long testEntityVersion = 4L;
  String entityId = "syn123";
  FileEntity file;

  @Before
  public void setup() {
    titleBar = new BasicTitleBar(mockView, mockGlobalAppState);

    folder = new Folder();
    folder.setId(entityId);
    folder.setName(testEntityName);

    file = new FileEntity();
    file.setId(entityId);
    file.setName(testEntityName);
    file.setVersionNumber(testEntityVersion);

    when(mockActionMenu.asWidget()).thenReturn(mockActionMenuWidget);
    when(mockActionMenu.getProps()).thenReturn(mockActionMenuProps);
    when(mockActionMenuProps.toJsInterop())
      .thenReturn(mockActionMenuJsInteropProps);
    when(mockBundle.getEntity()).thenReturn(folder);
    when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
  }

  @Test
  public void testAsWidget() {
    titleBar.asWidget();
  }

  @Test
  public void testConfigureBundleWithVersion() {
    when(mockBundle.getEntity()).thenReturn(file);
    titleBar.configure(mockBundle, mockActionMenu);
    verify(mockView).setProps(propsCaptor.capture());
    assertEquals(entityId, propsCaptor.getValue().getEntityId());
    assertEquals(
      testEntityVersion.longValue(),
      propsCaptor.getValue().getVersionNumber()
    );
    assertNotNull(propsCaptor.getValue().getEntityActionMenuProps());
    assertNotNull(
      propsCaptor.getValue().getOnActMemberClickAddConditionsForUse()
    );
  }

  @Test
  public void testConfigureBundleNoVersion() {
    titleBar.configure(mockBundle, mockActionMenu);
    verify(mockView).setProps(propsCaptor.capture());
    assertEquals(entityId, propsCaptor.getValue().getEntityId());
    assertEquals(0L, propsCaptor.getValue().getVersionNumber());
    assertNotNull(propsCaptor.getValue().getEntityActionMenuProps());
    assertNotNull(
      propsCaptor.getValue().getOnActMemberClickAddConditionsForUse()
    );
  }

  @Test
  public void testSetActionMenu() {
    // Set up the title bar and action menu
    titleBar.configure(mockBundle, mockActionMenu);

    verify(mockActionMenuWidget).removeFromParent();
    verify(mockActionMenu)
      .setPropUpdateListener(onActionMenuPropsChangeCaptor.capture());
    // Verify that the action menu props are passed to the view
    verify(mockView).setProps(propsCaptor.capture());
    assertEquals(
      mockActionMenuJsInteropProps,
      propsCaptor.getValue().getEntityActionMenuProps()
    );

    // Call under test: the action menu props have changed
    EntityActionMenuProps newPropsAfterUpdate = mock(
      EntityActionMenuProps.class
    );
    EntityActionMenuPropsJsInterop newJsInteropPropsAfterUpdate = mock(
      EntityActionMenuPropsJsInterop.class
    );
    when(mockActionMenu.getProps()).thenReturn(newPropsAfterUpdate);
    when(newPropsAfterUpdate.toJsInterop())
      .thenReturn(newJsInteropPropsAfterUpdate);

    // The actionMenu widget would pass the new props to the Consumer
    onActionMenuPropsChangeCaptor.getValue().accept(newPropsAfterUpdate);

    // Verify that the view is updated again when the runnable is called.
    verify(mockView, times(2)).setProps(propsCaptor.capture());
    assertEquals(
      newJsInteropPropsAfterUpdate,
      propsCaptor.getValue().getEntityActionMenuProps()
    );
  }

  @Test
  public void testAddActClickHandler() {
    titleBar.configure(mockBundle, mockActionMenu);
    verify(mockView).setProps(propsCaptor.capture());
    assertNotNull(
      propsCaptor.getValue().getOnActMemberClickAddConditionsForUse()
    );
    propsCaptor.getValue().getOnActMemberClickAddConditionsForUse().run();

    verify(mockPlaceChanger).goTo(placeCaptor.capture());
    assertEquals(
      AccessRequirementsPlace.class,
      placeCaptor.getValue().getClass()
    );
    assertEquals(
      entityId,
      placeCaptor.getValue().getParam(AccessRequirementsPlace.ID_PARAM)
    );
    assertEquals(
      RestrictableObjectType.ENTITY.toString(),
      placeCaptor.getValue().getParam(AccessRequirementsPlace.TYPE_PARAM)
    );
  }
}
