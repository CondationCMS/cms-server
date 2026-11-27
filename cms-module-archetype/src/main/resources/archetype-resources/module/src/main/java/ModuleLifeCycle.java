package ${package};

import com.condation.modules.api.Context;
import com.condation.modules.api.ModuleLifeCycleExtension;
import com.condation.modules.api.annotation.Extension;

/**
 * Example lifecycle extension: called when the module is activated and deactivated.
 * Use it to set up and release resources of your module.
 */
@Extension(ModuleLifeCycleExtension.class)
public class ModuleLifeCycle extends ModuleLifeCycleExtension<Context> {

    private static final System.Logger LOGGER = System.getLogger(ModuleLifeCycle.class.getName());

    @Override
    public void init() {
        // called once when the extension is created
    }

    @Override
    public void activate() {
        LOGGER.log(System.Logger.Level.INFO, "module ${moduleName} activated");
    }

    @Override
    public void deactivate() {
        LOGGER.log(System.Logger.Level.INFO, "module ${moduleName} deactivated");
    }
}
