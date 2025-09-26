/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.condation.cms.tests.injection.test2.example;

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

import com.condation.cms.tests.injection.test2.SimpleDIContainer;

/**
 *
 * @author thorstenmarx
 */
public class DIFrameworkDemo {
    public static void main(String[] args) {
        SimpleDIContainer container = new SimpleDIContainer();
        
        try {
            // Register components
            container.registerBean(EmailNotificationService.class);
            container.registerBean(SmsNotificationService.class);
            container.registerBean(DatabaseUserService.class);
            container.registerBean(OrderService.class);
            
            System.out.println(container.getStats());
            System.out.println("Registered beans: " + container.getBeanNames());
            
            // Use the services
            UserService userService = container.getBean(UserService.class);
            userService.createUser("John Doe");
            
            System.out.println("---");
            
            OrderService orderService = container.getBean(OrderService.class);
            orderService.processOrder("John Doe");
            
            System.out.println("---");
            
            // Get specific implementation
            NotificationService emailService = container.getBean(NotificationService.class, "emailService");
            emailService.sendNotification("Direct email");
            
            // Get primary bean (SMS service in this case)
            NotificationService primaryService = container.getBean(NotificationService.class);
            primaryService.sendNotification("Primary service message");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
