# CondationCMS

CondationCMS is a powerful and flexible content management system designed specifically for developers. 
It enables dynamic content creation and management through a modular architecture, powerful template engines, and flexible extensibility.

## ✨ Features

✅ Modular System: Extend and customize through flexible modules  
✅ Multi-Template Engine: Supports Freemarker, Thymeleaf, Velocity, and Pebble  
✅ Extensible Hook and Event Mechanisms  
✅ Integrated Caching  
✅ Multilingual Support  
✅ Multisite Support  

## Documentation

Detailed information on how to use and extend CondationCMS can be found in the official [documentation](https://condation.com/documentation).

## CondationCMS – Module & Extension Exception (Developer FAQ)

In short:

You may develop modules and extensions for CondationCMS under any license, including proprietary or commercial licenses.

The only requirement: your module or extension must interact exclusively through the official Module/Extension API.

You cannot copy or modify CondationCMS core code if you want your module/extension to remain proprietary.

Modules and extensions must be installable and removable without altering core files.

Any modifications to the core itself remain under the GPL.

Examples:

A module adding new functionality → can be closed-source or commercial.

An extension visualizing CMS data → can be sold commercially.

Modifying core classes (CoreEngine, ManagerUI) → GPL applies, even if a module is built alongside.

Developer shortcut:

“Anything that runs through the API can use your license. Anything touching the core stays GPL.”

## Changelog

[Changelog](CHANGELOG.md).
