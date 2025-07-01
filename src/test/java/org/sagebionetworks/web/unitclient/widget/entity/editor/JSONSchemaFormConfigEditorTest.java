package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.editor.JSONSchemaFormConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.JSONSchemaFormConfigView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class JSONSchemaFormConfigEditorTest {

  JSONSchemaFormConfigEditor editor;

  @Mock
  JSONSchemaFormConfigView mockView;

  WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
  String schemaUrl = "someUrl";
  String uiSchemaUrl = "someOtherUrl";
  String postUrl = "someThirdUrl";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    editor = new JSONSchemaFormConfigEditor(mockView);
  }

  @Test
  public void testConstructorAndEntitySelection() {
    verify(mockView).initView();
  }

  @Test
  public void testAsWidget() {
    editor.asWidget();
    verify(mockView).asWidget();
  }

  @Test
  public void testConfigure() {
    Map<String, String> descriptor = new HashMap<String, String>();
    descriptor.put(WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY, schemaUrl);
    descriptor.put(
      WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY,
      uiSchemaUrl
    );
    descriptor.put(WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY, postUrl);
    editor.configure(wikiKey, descriptor, null);

    verify(mockView).setSchemaUrl(schemaUrl);
    verify(mockView).setUiSchemaUrl(uiSchemaUrl);
    verify(mockView).setPostUrl(postUrl);
  }

  @Test
  public void testUpdateDescriptorFromView() {
    when(mockView.getSchemaUrl()).thenReturn(schemaUrl);
    when(mockView.getUiSchemaUrl()).thenReturn(uiSchemaUrl);
    when(mockView.getPostUrl()).thenReturn(postUrl);
    Map<String, String> descriptor = new HashMap<String, String>();
    editor.configure(wikiKey, descriptor, null);

    editor.updateDescriptorFromView();
    verify(mockView).getSchemaUrl();
    verify(mockView).getUiSchemaUrl();
    verify(mockView).getPostUrl();
    assertEquals(
      schemaUrl,
      descriptor.get(WidgetConstants.JSONSCHEMA_FORM_SCHEMA_URL_KEY)
    );
    assertEquals(
      uiSchemaUrl,
      descriptor.get(WidgetConstants.JSONSCHEMA_FORM_UI_SCHEMA_URL_KEY)
    );
    assertEquals(
      postUrl,
      descriptor.get(WidgetConstants.JSONSCHEMA_FORM_POST_URL_KEY)
    );
  }
}
