package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER;
import static org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl.TITLE_PREFIX;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.RenameEntityModalWidgetImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class RenameEntityModalWidgetTest {

  @Mock
  PromptForValuesModalView mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  CookieProvider mockCookies;

  @Mock
  Callback mockCallback;

  String startName;
  String startDescription;
  String entityDisplayType;
  RenameEntityModalWidgetImpl widget;
  Folder entity;
  TableEntity tableEntity;

  @Captor
  ArgumentCaptor<CallbackP<List<String>>> promptCallbackCaptor;

  @Captor
  ArgumentCaptor<Entity> entityCaptor;

  @Before
  public void before() {
    entity = new Folder();
    startName = "Start Name";
    entity.setName(startName);
    entityDisplayType = "Folder";
    widget =
      new RenameEntityModalWidgetImpl(mockView, mockJsClient, mockCookies);

    tableEntity = new TableEntity();
    tableEntity.setName(startName);
    tableEntity.setDescription(startDescription);
  }

  @Test
  public void testOnRename() {
    widget.onRename(entity, mockCallback);

    verify(mockView)
      .configureAndShow(
        eq(TITLE_PREFIX + entityDisplayType),
        eq(Arrays.asList("Name")),
        eq(Arrays.asList(startName)),
        eq(Arrays.asList(PromptForValuesModalView.InputType.TEXTBOX)),
        any(CallbackP.class)
      );
  }

  @Test
  public void testNullName() {
    widget.onRename(entity, mockCallback);

    verify(mockView)
      .configureAndShow(
        anyString(),
        anyList(),
        anyList(),
        anyList(),
        promptCallbackCaptor.capture()
      );
    promptCallbackCaptor.getValue().invoke(null);
    verify(mockView).showError(NAME_MUST_INCLUDE_AT_LEAST_ONE_CHARACTER);
    verify(mockJsClient, never())
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    // should only be called on success
    verify(mockCallback, never()).invoke();
  }

  @Test
  public void testNameNotChanged() {
    widget.onRename(entity, mockCallback);
    verify(mockView)
      .configureAndShow(
        anyString(),
        anyList(),
        anyList(),
        anyList(),
        promptCallbackCaptor.capture()
      );
    // Calling save with no real change just closes the dialog.
    promptCallbackCaptor.getValue().invoke(Arrays.asList(startName));
    verify(mockView, never()).setLoading(true);
    verify(mockView).hide();
    verify(mockJsClient, never())
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    // should only be called on success
    verify(mockCallback, never()).invoke();
  }

  @Test
  public void testRenameHappy() {
    String newName = "a new name";
    widget.onRename(entity, mockCallback);
    AsyncMockStubber
      .callSuccessWith(new TableEntity())
      .when(mockJsClient)
      .updateEntity(
        entityCaptor.capture(),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    verify(mockView)
      .configureAndShow(
        anyString(),
        anyList(),
        anyList(),
        anyList(),
        promptCallbackCaptor.capture()
      );
    promptCallbackCaptor.getValue().invoke(Arrays.asList(newName));

    assertEquals(entityCaptor.getValue().getName(), newName);
    assertEquals(
      entityCaptor.getValue().getDescription(),
      entity.getDescription()
    ); // Description shouldn't change

    verify(mockView).setLoading(true);
    verify(mockView).hide();
    verify(mockCallback).invoke();
  }

  @Test
  public void testRenameFailed() {
    Exception error = new Exception("an object already exists with that name");
    String newName = "a new name";
    widget.onRename(entity, mockCallback);
    AsyncMockStubber
      .callFailureWith(error)
      .when(mockJsClient)
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );

    verify(mockView)
      .configureAndShow(
        anyString(),
        anyList(),
        anyList(),
        anyList(),
        promptCallbackCaptor.capture()
      );
    promptCallbackCaptor.getValue().invoke(Arrays.asList(newName));

    verify(mockView).setLoading(true);
    verify(mockView).showError(error.getMessage());
    verify(mockView).setLoading(false);
    verify(mockView, never()).hide();
    verify(mockCallback, never()).invoke();
  }

  @Test
  public void testOnlyShowDescriptionForTables() {
    // Currently experimental mode only
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn("true");
    AsyncMockStubber
      .callSuccessWith(new TableEntity())
      .when(mockJsClient)
      .updateEntity(
        entityCaptor.capture(),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );

    String newDescription = "a new description";

    widget.onRename(tableEntity, mockCallback);
    verify(mockView)
      .configureAndShow(
        anyString(),
        eq(Arrays.asList("Name", "Description")),
        eq(Arrays.asList(startName, startDescription)),
        eq(
          Arrays.asList(
            PromptForValuesModalView.InputType.TEXTBOX,
            PromptForValuesModalView.InputType.TEXTAREA
          )
        ),
        promptCallbackCaptor.capture()
      );
    promptCallbackCaptor
      .getValue()
      .invoke(Arrays.asList(startName, newDescription));

    assertEquals(entityCaptor.getValue().getName(), startName); // Name should not have changed
    assertEquals(entityCaptor.getValue().getDescription(), newDescription);

    verify(mockView).setLoading(true);
    verify(mockView).hide();
    verify(mockCallback).invoke();
  }

  @Test
  public void testNullDescriptionWithNoUpdate() {
    tableEntity.setDescription(null);

    // Currently experimental mode only
    when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))
      .thenReturn("true");

    String newDescription = null;

    widget.onRename(tableEntity, mockCallback);
    verify(mockView)
      .configureAndShow(
        anyString(),
        eq(Arrays.asList("Name", "Description")),
        eq(Arrays.asList(startName, startDescription)),
        eq(
          Arrays.asList(
            PromptForValuesModalView.InputType.TEXTBOX,
            PromptForValuesModalView.InputType.TEXTAREA
          )
        ),
        promptCallbackCaptor.capture()
      );
    promptCallbackCaptor
      .getValue()
      .invoke(Arrays.asList(startName, newDescription));

    verify(mockJsClient, never())
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );

    // Nothing changed, so don't send an update
    verify(mockView, never()).setLoading(true);
    verify(mockView).hide();
    verify(mockJsClient, never())
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    // should only be called on success
    verify(mockCallback, never()).invoke();
  }
}
