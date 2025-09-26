/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.tests.injection.test1.sample;

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
