package org.sagebionetworks.web.unitclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.sagebionetworks.ResourceEncoder;
import org.sagebionetworks.ResourceUtils;
import org.sagebionetworks.repo.model.RegisterConstants;

/**
 * Load the register from the classpath.
 * @author John
 *
 */
public class RegisterConstantsStub implements RegisterConstants{

	@Override
	public String getRegisterJson() {
		String resource = "Register.json";
		InputStream in = ResourceEncoder.class.getClassLoader().getResourceAsStream(resource);
		if(in == null) throw new IllegalArgumentException("Cannot find "+resource+" on the classpath");
		try {
			String raw = ResourceUtils.readToString(in);
			return new String(Base64.encodeBase64(raw.getBytes("UTF-8")), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
