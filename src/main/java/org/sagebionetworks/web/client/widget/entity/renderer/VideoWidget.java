package org.sagebionetworks.web.client.widget.entity.renderer;

import static com.google.common.util.concurrent.Futures.getDone;
import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.sagebionetworks.web.client.DisplayConstants.*;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class VideoWidget implements WidgetRendererPresenter {

  public static final String VIMEO_URL_PREFIX =
    "https://player.vimeo.com/video/";
  public static final String YOUTUBE_URL_PREFIX =
    "https://www.youtube.com/embed/";
  private VideoWidgetView view;
  private Map<String, String> descriptor;
  AuthenticationController authenticationController;
  SynapseJavascriptClient jsClient;

  @Inject
  public VideoWidget(
    VideoWidgetView view,
    AuthenticationController authenticationController,
    SynapseJavascriptClient jsClient
  ) {
    this.view = view;
    this.authenticationController = authenticationController;
    this.jsClient = jsClient;
  }

  @Override
  public void configure(
    final WikiPageKey wikiKey,
    final Map<String, String> widgetDescriptor,
    Callback widgetRefreshRequired,
    Long wikiVersionInView
  ) {
    this.descriptor = widgetDescriptor;

    String youTubeVideoId = descriptor.get(
      WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY
    );
    String vimeoVideoId = descriptor.get(
      WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY
    );
    String mp4SynapseId = descriptor.get(
      WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY
    );
    String oggSynapseId = descriptor.get(
      WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY
    );
    String webmSynapseId = descriptor.get(
      WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY
    );
    String width = descriptor.get(WidgetConstants.VIDEO_WIDGET_WIDTH_KEY);
    String height = descriptor.get(WidgetConstants.HEIGHT_KEY);
    if (youTubeVideoId != null) {
      view.configure(YOUTUBE_URL_PREFIX + youTubeVideoId);
    } else if (vimeoVideoId != null) {
      view.configure(VIMEO_URL_PREFIX + vimeoVideoId);
    } else {
      configureFromSynapseFile(
        mp4SynapseId,
        oggSynapseId,
        webmSynapseId,
        width,
        height
      );
    }
    descriptor = widgetDescriptor;
  }

  public void configure(
    String synapseId,
    String filename,
    int width,
    int height
  ) {
    String mp4SynapseId = VideoConfigEditor.isRecognizedMP4FileName(filename)
      ? synapseId
      : null;
    String oggSynapseId = VideoConfigEditor.isRecognizedOggFileName(filename)
      ? synapseId
      : null;
    String webmSynapseId = VideoConfigEditor.isRecognizedWebMFileName(filename)
      ? synapseId
      : null;
    configureFromSynapseFile(
      mp4SynapseId,
      oggSynapseId,
      webmSynapseId,
      Integer.toString(width),
      Integer.toString(height)
    );
  }

  private void configureFromSynapseFile(
    String mp4SynapseId,
    String oggSynapseId,
    String webmSynapseId,
    String width,
    String height
  ) {
    EntityBundleRequest request = new EntityBundleRequest();
    request.setIncludeFileHandles(true);
    List<ListenableFuture<EntityBundle>> entityBundleCalls = new ArrayList<ListenableFuture<EntityBundle>>();
    if (mp4SynapseId != null) {
      entityBundleCalls.add(jsClient.getEntityBundle(mp4SynapseId, request));
    }
    if (oggSynapseId != null) {
      entityBundleCalls.add(jsClient.getEntityBundle(oggSynapseId, request));
    }
    if (webmSynapseId != null) {
      entityBundleCalls.add(jsClient.getEntityBundle(webmSynapseId, request));
    }

    FluentFuture
      .from(
        whenAllComplete(entityBundleCalls)
          .call(
            () -> {
              // Retrieve the resolved values from the futures
              for (ListenableFuture<EntityBundle> future : entityBundleCalls) {
                EntityBundle bundle = getDone(future);
                if (bundle.getFileHandles().isEmpty()) {
                  String errorMessage = authenticationController.isLoggedIn()
                    ? ERROR_FAILURE_PRIVLEDGES
                    : ERROR_LOGIN_REQUIRED;
                  view.showError(errorMessage);
                  return null;
                }
              }
              // otherwise, we found a file handle for each video FileEntity, so show!
              view.configure(
                mp4SynapseId,
                oggSynapseId,
                webmSynapseId,
                width,
                height
              );
              return null;
            },
            directExecutor()
          )
      )
      .catching(
        Throwable.class,
        e -> {
          view.showError(e.getMessage());
          return null;
        },
        directExecutor()
      );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
