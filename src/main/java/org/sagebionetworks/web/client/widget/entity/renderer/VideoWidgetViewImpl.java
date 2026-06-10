package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

public class VideoWidgetViewImpl extends FlowPanel implements VideoWidgetView {

  private SynapseJSNIUtils synapseJsniUtils;

  @Inject
  public VideoWidgetViewImpl(SynapseJSNIUtils synapseJsniUtils) {
    this.synapseJsniUtils = synapseJsniUtils;
  }

  @Override
  public void configure(
    String mp4SynapseId,
    String oggSynapseId,
    String webmSynapseId,
    String vttSynapseId,
    String width,
    String height
  ) {
    this.clear();

    // Use the provided dimensions, defaulting to 640x480 if not specified.
    // max-width:100% allows the video to shrink below its nominal width to fill
    // smaller containers (e.g. the Files tab preview pane) while respecting the
    // aspect ratio automatically.
    String w = (width != null) ? SafeHtmlUtils.htmlEscape(width) : "640";
    String h = (height != null) ? SafeHtmlUtils.htmlEscape(height) : "480";

    StringBuilder builder = new StringBuilder();
    builder.append(
      "<video width=\"" +
      w +
      "\" height=\"" +
      h +
      "\" style=\"max-width:100%;height:auto;\" controls crossorigin=\"anonymous\">"
    );
    if (mp4SynapseId != null) {
      builder.append("<source src=\"");
      builder.append(
        DisplayUtils.createFileEntityUrl(
          synapseJsniUtils.getBaseFileHandleUrl(),
          mp4SynapseId,
          null,
          false
        )
      );
      builder.append("\" type=\"video/mp4\">");
    }

    if (oggSynapseId != null) {
      builder.append("<source src=\"");
      builder.append(
        DisplayUtils.createFileEntityUrl(
          synapseJsniUtils.getBaseFileHandleUrl(),
          oggSynapseId,
          null,
          false
        )
      );
      builder.append("\" type=\"video/ogg\">");
    }

    if (webmSynapseId != null) {
      builder.append("<source src=\"");
      builder.append(
        DisplayUtils.createFileEntityUrl(
          synapseJsniUtils.getBaseFileHandleUrl(),
          webmSynapseId,
          null,
          false
        )
      );
      builder.append("\" type=\"video/webm\">");
    }

    // track item for captions, if vtt is provided
    if (vttSynapseId != null) {
      builder.append("<track kind=\"subtitles\" src=\"");
      builder.append(
        DisplayUtils.createFileEntityUrl(
          synapseJsniUtils.getBaseFileHandleUrl(),
          vttSynapseId,
          null,
          false
        )
      );
      builder.append("\" srclang=\"en\" label=\"English\" default>");
    }
    // alt text if the browser does not support
    builder.append("Your browser does not support the video tag.");

    builder.append("</video>");
    add(new HTML(builder.toString()));
  }

  @Override
  public void configure(String iframeTargetUrl) {
    clear();

    StringBuilder sb = new StringBuilder();
    sb.append(
      "<iframe width=\"560\" height=\"315\" frameborder=\"0\" allowfullscreen=\"true\" src=\""
    );
    sb.append(SafeHtmlUtils.htmlEscape(iframeTargetUrl));

    sb.append("\" />");
    add(new HTML(sb.toString()));
  }

  @Override
  public void showError(String error) {
    clear();
    add(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(error)));
  }

  @Override
  public Widget asWidget() {
    return this;
  }
}
