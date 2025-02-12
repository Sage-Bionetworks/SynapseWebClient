package org.sagebionetworks.web.server.servlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

/**
 * This class is used to provide the HTML scripts required by Vite.
 */
public class ViteHTMLProvider {

  private static final String VITE_DEV_SERVER_URL = "http://localhost:5173";

  private static final String VITE_CLIENT_PACKAGE = "@vite/client";

  /**
   * This method returns the HTML script required by Vite.
   *
   * See https://vite.dev/guide/backend-integration
   *
   * @return the HTML script required by Vite
   */
  public static String getViteDevelopmentHTML(List<String> filesToImport) {
    StringBuilder html = new StringBuilder();
    html.append(
      "<script type=\"module\" src=\"" +
      VITE_DEV_SERVER_URL +
      "/" +
      VITE_CLIENT_PACKAGE +
      "\"></script>\n"
    );

    filesToImport.forEach(file ->
      html.append(
        "<script type=\"module\" src=\"" +
        VITE_DEV_SERVER_URL +
        "/" +
        file +
        "\"></script>\n"
      )
    );

    return html.toString();
  }

  private static List<JSONObject> getImportedChunks(
    JSONObject chunk,
    JSONObject manifest,
    Set<String> seen
  ) {
    List<JSONObject> chunks = new ArrayList<>();
    if (chunk.has("imports")) {
      chunk
        .getJSONArray("imports")
        .forEach(file -> {
          String filePath = (String) file;
          JSONObject importee = manifest.getJSONObject(filePath);
          if (!seen.contains(filePath)) {
            seen.add(filePath);
            chunks.addAll(getImportedChunks(importee, manifest, seen));
            chunks.add(importee);
          }
        });
    }

    return chunks;
  }

  private static List<JSONObject> importedChunks(
    JSONObject manifest,
    String name
  ) {
    return getImportedChunks(
      manifest.getJSONObject(name),
      manifest,
      new HashSet<>()
    );
  }

  public static String getViteProductionHTML(
    List<String> filesToImport,
    JSONObject manifest,
    String assetPath
  ) {
    StringBuilder html = new StringBuilder();

    // For each imported file, add a `link` tag for each CSS file
    filesToImport.forEach(file -> {
      JSONObject entry = manifest.getJSONObject(file);
      if (entry.has("css")) {
        entry
          .getJSONArray("css")
          .forEach(cssFile ->
            html.append(
              "<link rel=\"stylesheet\" href=\"" +
              assetPath +
              cssFile +
              "\" />\n"
            )
          );
      }

      // For imported chunks, add a `link` tag for each CSS file
      List<JSONObject> importedChunks = importedChunks(manifest, file);
      importedChunks.forEach(chunk -> {
        if (chunk.has("css")) {
          chunk
            .getJSONArray("css")
            .forEach(cssFile ->
              html.append(
                "<link rel=\"stylesheet\" href=\"" +
                assetPath +
                cssFile +
                "\" />\n"
              )
            );
        }
      });

      // Add the script tag for the file
      html.append(
        "<script type=\"module\" src=\"" +
        assetPath +
        manifest.getJSONObject(file).getString("file") +
        "\"></script>\n"
      );

      // For each imported chunk, add a `link` tag for the modulepreload
      importedChunks.forEach(chunk ->
        html.append(
          "<link rel=\"modulepreload\" href=\"" +
          assetPath +
          chunk.getString("file") +
          "\" />\n"
        )
      );
    });

    return html.toString();
  }
}
