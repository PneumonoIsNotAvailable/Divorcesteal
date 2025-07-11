package net.pneumono.divorcesteal.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class DivorcestealEnPtProvider extends FabricLanguageProvider {
    public DivorcestealEnPtProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_pt", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder);
        String modId = Divorcesteal.MOD_ID;

        builder.add(DivorcestealRegistry.HEART_ITEM, "Pirate's Heart");
        builder.add("item.divorcesteal.heart.crafted", "Craft'd");
        builder.add(DivorcestealRegistry.REVIVE_BEACON_ITEM, "Beacon o' Rescue");
        builder.add("item.divorcesteal.revive_beacon.target", "Yer Target: %s");
        builder.add("item.divorcesteal.player_head.killer", "Plunder'd %s");

        builder.add("divorcesteal.gui.deathban.title", "Ye have not a heart more!");
        builder.add("divorcesteal.gui.revive_beacon.title", "Beacon o' Rescue");
        builder.add("divorcesteal.gui.revive_beacon.wanted", "WANTED");
        builder.add("divorcesteal.gui.revive_beacon.add_heart", "Add yer Pirate's Heart");
        builder.add("divorcesteal.gui.revive_beacon.add_head", "Add yer Sailor Skull");
        builder.add("divorcesteal.gui.revive_beacon.revive", "Rescue");

        builder.add(DivorcestealRegistry.USE_HEART_SOUND, "Pirate's Heart applies");
        builder.add(DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, "Beacon o' Rescue triggers");
        builder.add(DivorcestealRegistry.DEATHBAN_SOUND, "Deathban echoes");
        builder.add(DivorcestealRegistry.REVIVE_SOUND, "Rescue echoes");

        builder.add("divorcesteal.unknown", "???");
        builder.add("divorcesteal.deathban", "Ye have not a heart more!");
        builder.add("divorcesteal.deathban_global", "%s has not a heart more!");

        builder.add(DivorcestealRegistry.STEAL_LIFE_STAT, "Hearts Plundered", "stat");
        builder.add(DivorcestealRegistry.WITHDRAW_HEART_STAT, "Hearts Withdrawn", "stat");
        builder.add(DivorcestealRegistry.REVIVE_PLAYER_STAT, "Mateys saved from Davy Jones", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_PLAYER_STAT, "Sailors sent t' Davy Jones", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_SELF_STAT, "Times sent t' Davy Jones", "stat");

        builder.add("commands.divorcesteal.get", "%1$s be hoardin' %2$s hearts");
        builder.add("commands.divorcesteal.set.single", "%1$s be forced to %2$s hearts");
        builder.add("commands.divorcesteal.set.multiple", "%1$s sailors be forced to %2$s hearts");
        builder.add("commands.divorcesteal.add.single", "%2$s be gifted %1$s hearts");
        builder.add("commands.divorcesteal.add.multiple", "%2$s sailors be gifted %1$s hearts");
        builder.add("commands.divorcesteal.remove.single", "Plundered %1$s hearts from %2$s");
        builder.add("commands.divorcesteal.remove.multiple", "Plundered %1$s hearts from %2$s players");
        builder.add("commands.divorcesteal.revive.single", "Rescued %s from Davy Jones' Locker");
        builder.add("commands.divorcesteal.revive.multiple", "Rescued %s sailors from Davy Jones' Locker");
        builder.add("commands.divorcesteal.delete.single", "Shipwrecked logs for %s");
        builder.add("commands.divorcesteal.delete.multiple", "Shipwrecked logs for %s sailors");
        builder.add("commands.divorcesteal.withdraw.single", "Withdrew 1 heart");
        builder.add("commands.divorcesteal.withdraw.multiple", "Withdrew %s hearts");
        builder.add("commands.divorcesteal.withdraw.fail", "Ye can't withdraw any further 'earts!");

        builder.add("arguments.divorcesteal.error.no_data", "No logs exist fer that selection o' sailors!");
        builder.add("arguments.divorcesteal.error.not_deathbanned", "Ye can't rescue a sailor if they were never wi' Davy Jones in th' first place!");

        builder.addConfigScreenTitle(modId, "Divorceplunder Ship Log");
        builder.addConfig(DivorcestealConfig.MAX_HEARTS,
                "Greatest Hearts",
                "Th' greatest share o' hearts a crew member can 'ave"
        );
        builder.addConfig(DivorcestealConfig.DEFAULT_HEARTS,
                "Startin' Hearts",
                "Th' share o' hearts sailors start with when they first join th' crew"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_HEARTS,
                "Returnin' Hearts",
                "Th' share o' hearts sailors keep when returnin' from Davy Jones"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_DAYS,
                "Moons to Return",
                "How many real-life days it takes fer Davy Jones to set ye free. -1 means ye be lost forever!"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_ELYTRA,
                "Prohibit Icarus' Wings",
                "If yer Icarus' Wings be unable to take flight"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_TOTEMS,
                "Prohibit Jewels o' Life",
                "If yer Jewels o' Life be unable to save ye"
        );
        builder.addConfigCategory(modId, "hearts", "Hearts");
        builder.addConfigCategory(modId, "rebalances", "Laws");

        builder.addModMenuTranslations(modId,
                "Divorceplunder",
                "Th' official mod fer Divorceplunder",
                "Th' official mod fer Divorceplunder, not affiliated wi' th' SMP o' Stealin' Lives\n\nNow wi' support fer yer Pirate Parlance!"
        );
    }
}
