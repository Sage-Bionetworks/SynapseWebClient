package org.sagebionetworks.web.unitserver;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.LayoutServiceImpl;

public class LayoutServiceImplTest {

	private LayoutServiceImpl layoutService;
	
	@Before
	public void setup() {	
		layoutService = new LayoutServiceImpl();
	}
	
}
