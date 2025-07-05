package net.pneumono.divorcesteal;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.pneumono.pneumonocore.PneumonoCoreModMenu;

public class DivorcestealModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PneumonoCoreModMenu.getModConfigScreenFactory(Divorcesteal.MOD_ID);
    }
}
