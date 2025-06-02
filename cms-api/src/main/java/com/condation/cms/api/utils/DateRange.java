package com.condation.cms.api.utils;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2025 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.time.Instant;
import java.util.Date;

public class DateRange {

    

    /**
     * Checks if the current UTC time is within the range.
	 * @param from
	 * @param to
	 * @return 
     */
    public static boolean isNowWithin(Date from, Date to) {
        
		var now = Date.from(Instant.now());
		
		if ( from != null && !(from.before(now) || from.equals(now)) ) {
			return false;
		}
		if (to != null
				&& (to.before(now) || to.equals(now))) {
			return false;
		}
		return true;
    }

    
}
