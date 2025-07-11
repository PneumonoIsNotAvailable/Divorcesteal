package net.pneumono.divorcesteal.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class DivorcestealLolUsProvider extends FabricLanguageProvider {
    public DivorcestealLolUsProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "lol_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder);
        String modId = Divorcesteal.MOD_ID;

        builder.add(DivorcestealRegistry.HEART_ITEM, "Cat Hart");
        builder.add("item.divorcesteal.heart.crafted", "Craftd");
        builder.add(DivorcestealRegistry.REVIVE_BEACON_ITEM, "Com Bak Shiny");
        builder.add("item.divorcesteal.revive_beacon.target", "Murdr: %s");
        builder.add("item.divorcesteal.player_head.killer", "Ded from %s");

        builder.add("divorcesteal.gui.deathban.title", "U haz no harts!!");
        builder.add("divorcesteal.gui.revive_beacon.title", "Com Bak Shiny");
        builder.add("divorcesteal.gui.revive_beacon.wanted", "I HATEZ");
        builder.add("divorcesteal.gui.revive_beacon.add_heart", "Putt hart");
        builder.add("divorcesteal.gui.revive_beacon.add_head", "Putt Kat Hed");
        builder.add("divorcesteal.gui.revive_beacon.revive", "Bring Bak");

        builder.add(DivorcestealRegistry.USE_HEART_SOUND, "Hart eated");
        builder.add(DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, "Com Back Shiny brings kat bak!");
        builder.add(DivorcestealRegistry.DEATHBAN_SOUND, "Dedban loud :(");
        builder.add(DivorcestealRegistry.REVIVE_SOUND, "Kat return loud :(");

        builder.add("divorcesteal.unknown", "???");
        builder.add("divorcesteal.deathban", "U haz no harts!!");
        builder.add("divorcesteal.deathban_global", "%s haz no harts!!");

        builder.add(DivorcestealRegistry.STEAL_LIFE_STAT, "Hartz stealed", "stat");
        builder.add(DivorcestealRegistry.WITHDRAW_HEART_STAT, "Hartz wifdrawd", "stat");
        builder.add(DivorcestealRegistry.REVIVE_PLAYER_STAT, "Kittiez bringed bak!", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_PLAYER_STAT, "Kittiez dedband", "stat");
        builder.add(DivorcestealRegistry.DEATHBAN_SELF_STAT, "Times u got Dedband", "stat");

        builder.add("commands.divorcesteal.get", "%1$s haz %2$s hartz!");
        builder.add("commands.divorcesteal.set.single", "Kat %1$s haz %2$s harts now");
        builder.add("commands.divorcesteal.set.multiple", "%1$s katz hav %2$s harts now");
        builder.add("commands.divorcesteal.add.single", "Giv %1$s harts to %2$s");
        builder.add("commands.divorcesteal.add.multiple", "Giv %1$s harts to %2$s kittehz");
        builder.add("commands.divorcesteal.remove.single", "Tuk %1$s harts frm %2$s");
        builder.add("commands.divorcesteal.remove.multiple", "Tuk %1$s harts frm %2$s kittehz");
        builder.add("commands.divorcesteal.revive.single", "Bringd bak %s");
        builder.add("commands.divorcesteal.revive.multiple", "Bringd bak %s cats");
        builder.add("commands.divorcesteal.delete.single", "Destryed stuff abt %s");
        builder.add("commands.divorcesteal.delete.multiple", "Destryed stuff abt %s kats");
        builder.add("commands.divorcesteal.withdraw.single", "Tuk out 1 hart");
        builder.add("commands.divorcesteal.withdraw.multiple", "Tuk out %s hartz");
        builder.add("commands.divorcesteal.withdraw.fail", "Cant take more or u diez!!");

        builder.add("arguments.divorcesteal.error.no_data", "Dont have stuff 4 those kittehs!");
        builder.add("arguments.divorcesteal.error.not_deathbanned", "Cant bring bak a cat if they not gon!");

        builder.addConfigScreenTitle(modId, "Divorcesteal Changez");
        builder.addConfig(DivorcestealConfig.MAX_HEARTS,
                "BIG Harts",
                "Biggest hartz a cat can has!"
        );
        builder.addConfig(DivorcestealConfig.DEFAULT_HEARTS,
                "Norml Harts",
                "Hartz cat has wen they joinz"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_HEARTS,
                "Bringbak Harts",
                "Hartz can has wen they get bringed bak"
        );
        builder.addConfig(DivorcestealConfig.REVIVE_DAYS,
                "Sleeps 2 bringbak",
                "How many timez u need 2 sleep irl 2 bring bak cats. -1 means no bringing bak!!"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_ELYTRA,
                "No mor fly :(",
                "If flyin wings arnt alowed"
        );
        builder.addConfig(DivorcestealConfig.DISABLE_TOTEMS,
                "No mor totum :(",
                "If totum not alowed"
        );
        builder.addConfigCategory(modId, "hearts", "Harts");
        builder.addConfigCategory(modId, "rebalances", "Changez");

        builder.addModMenuTranslations(modId,
                "evil surgery demons",
                "OFFICIAL codin thing for divorse steel",
                "The ofishul coded thingy for divorse steel, NOT frens wit Life stealin catz\n\nKan be playd if ur a cat!!"
        );
    }
}
