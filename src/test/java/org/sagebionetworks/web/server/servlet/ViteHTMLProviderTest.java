package org.sagebionetworks.web.server.servlet;

import java.io.InputStream;
import java.util.List;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ViteHTMLProviderTest extends TestCase {

  public void testGetViteDevelopmentHTML() {
    String html = ViteHTMLProvider.getViteDevelopmentHTML(
      List.of("js/main.js")
    );

    assertEquals(
      "<script type=\"module\" src=\"http://localhost:5173/@vite/client\"></script>\n" +
      "<script type=\"module\" src=\"http://localhost:5173/js/main.js\"></script>\n",
      html
    );
  }

  public void testGetViteProductionHTML() {
    // Test matches the example at https://vite.dev/guide/backend-integration

    InputStream exampleManifestAsStream = getClass()
      .getClassLoader()
      .getResourceAsStream("example-manifest.json");
    JSONObject manifest = new JSONObject(
      new JSONTokener(exampleManifestAsStream)
    );

    String fooHtml = ViteHTMLProvider.getViteProductionHTML(
      List.of("views/foo.js"),
      manifest,
      ""
    );

    assertEquals(
      "<link rel=\"stylesheet\" href=\"assets/foo-5UjPuW-k.css\" />\n" +
      "<link rel=\"stylesheet\" href=\"assets/shared-ChJ_j-JJ.css\" />\n" +
      "<script type=\"module\" src=\"assets/foo-BRBmoGS9.js\"></script>\n" +
      "<link rel=\"modulepreload\" href=\"assets/shared-B7PI925R.js\" />\n",
      fooHtml
    );

    String barHtml = ViteHTMLProvider.getViteProductionHTML(
      List.of("views/bar.js"),
      manifest,
      ""
    );

    assertEquals(
      "<link rel=\"stylesheet\" href=\"assets/shared-ChJ_j-JJ.css\" />\n" +
      "<script type=\"module\" src=\"assets/bar-gkvgaI9m.js\"></script>\n" +
      "<link rel=\"modulepreload\" href=\"assets/shared-B7PI925R.js\" />\n",
      barHtml
    );
  }
}
