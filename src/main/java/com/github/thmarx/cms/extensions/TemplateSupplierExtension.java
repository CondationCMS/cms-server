/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.thmarx.cms.extensions;

import java.util.function.Supplier;

/**
 *
 * @author t.marx
 */
public record TemplateSupplierExtension (String name, Supplier<?> supplier) {}
