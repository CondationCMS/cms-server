package com.condation.cms.configuration.reload;

import com.condation.cms.configuration.IConfiguration;
import com.condation.cms.configuration.ReloadStrategy;

/**
 *
 * @author t.marx
 */
public class NoReload implements ReloadStrategy {

	@Override
	public void register(IConfiguration configuration) {
	}	
}
