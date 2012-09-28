package org.sagebionetworks.web.client.utils;

public enum APPROVAL_REQUIRED {
		NONE, // i.e. OPEN
		LICENSE_ACCEPTANCE, // i.e. RESTRICTED (Terms of Use)
		ACT_APPROVAL // i.e. CONTROLLED (ACT Approval required)
}
