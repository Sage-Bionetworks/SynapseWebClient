package org.sagebionetworks.web.shared;

import java.util.List;
import java.util.Map;

public class KeyValueDisplay<T> {

	private Map<String, T> map;
	private List<String> keyDisplayOrder;

	public KeyValueDisplay(Map<String, T> map, List<String> keyDisplayOrder) {
		super();
		this.map = map;
		this.keyDisplayOrder = keyDisplayOrder;
	}

	public Map<String, T> getMap() {
		return map;
	}

	public void setMap(Map<String, T> map) {
		this.map = map;
	}

	public List<String> getKeyDisplayOrder() {
		return keyDisplayOrder;
	}

	public void setKeyDisplayOrder(List<String> keyDisplayOrder) {
		this.keyDisplayOrder = keyDisplayOrder;
	}

}
