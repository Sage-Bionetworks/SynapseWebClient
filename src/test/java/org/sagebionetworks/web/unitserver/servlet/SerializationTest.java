package org.sagebionetworks.web.unitserver.servlet;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.servlet.openid.SampleConsumer;

public class SerializationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test 
	public void testSerialization() throws Exception {
		List<Integer> o = new ArrayList<Integer>();
		o.add(1);
		o.add(2);
		o.add(100);
		o.add(-10000);
		
		String e = SampleConsumer.encryptingSerializer(o);

		ArrayList<Integer> o2 = SampleConsumer.decryptingDeserializer(e);
		assertEquals(o, o2);
	}

}
