package com.dread;

import com.dread.command.DreadCommands;
import com.dread.event.DreadDirector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DreadMod implements ModInitializer {
    public static final String MOD_ID = "dread";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModEntities.init();
        ModItems.init();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> DreadCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(DreadDirector::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DreadDirector.reset());

        LOGGER.info("Dread has awoken. Something is watching.");
    }
}
