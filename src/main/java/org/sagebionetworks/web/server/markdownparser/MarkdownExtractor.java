package org.sagebionetworks.web.server.markdownparser;

import java.util.Map;
import java.util.Set;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

import com.google.gwt.dev.util.collect.HashMap;

public class MarkdownExtractor {
	private Map<String, String> containerToContents;
	private int idCount;
	
	public MarkdownExtractor() {
		containerToContents = new HashMap<String, String>();
		idCount = 0;
	}
	
	public int getCurrentContainerId() {
		return idCount;
	}
	
	public String getContainerElementStart() {
		return ServerMarkdownUtils.START_CONTAINER;
	}
	
	public String getContainerElementEnd() {
		//Ended element; increment for next element
		idCount++;
		return ServerMarkdownUtils.END_CONTAINER;
	}
	
	public String getNewElementStart(String currentDivId) {
		StringBuilder sb = new StringBuilder();
		sb.append(getContainerElementStart() + currentDivId);
		sb.append("\">");
		return sb.toString();
	}
	
	public void putContainerIdToContent(String containerId, String content) {
		containerToContents.put(containerId, content);
	}
	
	public Set<String> getContainerIds() {
		return containerToContents.keySet();
	}
	
	public String getContent(String containerId) {
		return containerToContents.get(containerId);
	}
	
	public Map<String, String> getMap() {
		return containerToContents;
	}
	
}
