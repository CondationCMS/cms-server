---
title: Modules
template: start.html
menu: 
    position: 15
    title: "Modules"
---

## Modules

Modules are the way to provide functionality without change the core all the time.
Moduels are installed global per server, but activated per host. 
If a module stores custom data, each module has a custom data directory per host.


**Activate a module**
```yaml
modules:
  active:
    - example-module
    - flexmark-module
```

