package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class GovernanceMarkdownGithubProps extends ReactComponentProps {

  @JsNullable
  public String repoOwner;

  @JsNullable
  public String repoName;

  @JsNullable
  public String filePath;

  @JsNullable
  public boolean showDownloadButton;

  @JsOverlay
  public static GovernanceMarkdownGithubProps create(
    String repoOwner,
    String repoName,
    String filePath,
    boolean showDownloadButton
  ) {
    GovernanceMarkdownGithubProps props = new GovernanceMarkdownGithubProps();
    props.repoOwner = repoOwner;
    props.repoName = repoName;
    props.filePath = filePath;
    props.showDownloadButton = showDownloadButton;
    return props;
  }
}
