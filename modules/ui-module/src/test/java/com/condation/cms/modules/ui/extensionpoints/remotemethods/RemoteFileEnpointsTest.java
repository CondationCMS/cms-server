package com.condation.cms.modules.ui.extensionpoints.remotemethods;

import com.condation.cms.api.module.CMSModuleContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author thmar
 */
@ExtendWith(MockitoExtension.class)
public class RemoteFileEnpointsTest {
	
	@Mock
	CMSModuleContext moduleContext;
	
	
	public RemoteFileEnpointsTest() {
	}

	@Test
	public void test_create_file() {
		RemoteFileEnpoints fileEndpoints = new RemoteFileEnpoints();
		fileEndpoints.setContext(moduleContext);
		
		
	}
	
}
