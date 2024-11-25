package com.condation.cms.templates.parser;

/*-
 * #%L
 * templates
 * %%
 * Copyright (C) 2023 - 2024 CondationCMS
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

import java.util.ArrayList;
import java.util.List;

public class TagNode extends ASTNode {
    private String name;
    private String condition;

	public TagNode () {
		
	}

	public TagNode(String name) {
		this.name = name;
	}
	
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCondition (String condition) {
        this.condition = condition;
    }

    public String getCondition () {
        return condition;
    }

    @Override
    public String toString() {
        return "TagNode('" + name + ", " + condition + "')";
    }
}
