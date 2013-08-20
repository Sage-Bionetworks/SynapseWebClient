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
		idCount = -1;
	}
	
	public int getCurrentContainerId() {
		return idCount;
	}
	
	public String getContainerElementStart() {
		//Starting a new container increments the id counter
		idCount++;
		return ServerMarkdownUtils.START_CONTAINER;
	}
	
	public String getContainerElementEnd() {
		return ServerMarkdownUtils.END_CONTAINER;
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
