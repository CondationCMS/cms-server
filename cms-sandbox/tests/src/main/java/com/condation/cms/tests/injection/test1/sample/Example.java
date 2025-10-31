/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.tests.injection.test1.sample;

/*-
 * #%L
 * tests
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

import com.condation.cms.tests.injection.test1.AllowMultiple;
import com.condation.cms.tests.injection.test1.SimpleDIContainer;

/**
 *
 * @author thorstenmarx
 */
public class Example {
	// Example usage
    public static void main(String[] args) {
        SimpleDIContainer c = new SimpleDIContainer();

        // register interface implementations
        c.register(Service.class, ServiceImpl1.class);
        c.register(Service.class, ServiceImpl2.class);

        for (Service s : c.getBeans(Service.class)) {
            s.execute();
        }

        c.debugDump();
    }

    public interface Service {
        void execute();
    }

    @AllowMultiple
    public static class ServiceImpl1 implements Service {
        public void execute() { System.out.println("ServiceImpl1 executed"); }
    }

    @AllowMultiple
    public static class ServiceImpl2 implements Service {
        public void execute() { System.out.println("ServiceImpl2 executed"); }
    }
}
