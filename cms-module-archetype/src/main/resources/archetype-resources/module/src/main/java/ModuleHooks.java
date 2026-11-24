package ${package};

import ${package}.api.ModuleMessage;
import com.condation.cms.api.annotations.Action;
import com.condation.cms.api.extensions.HookSystemRegisterExtensionPoint;
import com.condation.modules.api.annotation.Extension;

@Extension(HookSystemRegisterExtensionPoint.class)
public class ModuleHooks extends HookSystemRegisterExtensionPoint {

    @Action("module/${artifactId}/hello")
    public String hello() {
        return new ModuleMessage("Hello from ${moduleName}").message();
    }
}
