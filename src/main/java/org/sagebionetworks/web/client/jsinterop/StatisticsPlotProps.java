package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class StatisticsPlotProps extends ReactComponentProps {

  @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
  public static class ProjectFilesStatisticsRequest {

    public String concreteType;
    public String objectId;
    public boolean fileDownloads;
    public boolean fileUploads;
  }

  public ProjectFilesStatisticsRequest request;
  public String title;

  @JsOverlay
  public static StatisticsPlotProps create(String projectId, String title) {
    ProjectFilesStatisticsRequest request = new ProjectFilesStatisticsRequest();
    request.concreteType =
      "org.sagebionetworks.repo.model.statistics.ProjectFilesStatisticsRequest";
    request.objectId = projectId;
    request.fileDownloads = true;
    request.fileUploads = true;

    StatisticsPlotProps props = new StatisticsPlotProps();
    props.request = request;
    props.title = title;
    return props;
  }
}
