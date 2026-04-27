package com.smalldaydc.friendlycreeper.client;

import com.smalldaydc.friendlycreeper.FriendlyCreeperConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FriendlyCreeperConfigScreen {

    public static Screen create(Screen parent) {
        FriendlyCreeperConfig config = FriendlyCreeperConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.friendlycreeper.title"))
                .setSavingRunnable(FriendlyCreeperConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.friendlycreeper.category.general"));
        ConfigCategory client = builder.getOrCreateCategory(Text.translatable("config.friendlycreeper.category.client"));

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.allowOwnerDamage"),
                        config.allowOwnerDamage)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.friendlycreeper.allowOwnerDamage.tooltip"))
                .setSaveConsumer(value -> config.allowOwnerDamage = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.followOwner"),
                        config.followOwner)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.followOwner.tooltip"))
                .setSaveConsumer(value -> config.followOwner = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.revengeOwner"),
                        config.revengeOwner)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.revengeOwner.tooltip"))
                .setSaveConsumer(value -> config.revengeOwner = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.snowGolemAttack"),
                        config.snowGolemAttack)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.friendlycreeper.snowGolemAttack.tooltip"))
                .setSaveConsumer(value -> config.snowGolemAttack = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.afraidOfCats"),
                        config.afraidOfCats)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.afraidOfCats.tooltip"))
                .setSaveConsumer(value -> config.afraidOfCats = value)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.witherRoseOnLowHealth"),
                        config.witherRoseOnLowHealth)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.witherRoseOnLowHealth.tooltip"))
                .setSaveConsumer(value -> config.witherRoseOnLowHealth = value)
                .build());

        client.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.hurtSound"),
                        config.hurtSound)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.hurtSound.tooltip"))
                .setSaveConsumer(value -> config.hurtSound = value)
                .build());

        client.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("config.friendlycreeper.renderPoppy"),
                        config.renderPoppy)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.friendlycreeper.renderPoppy.tooltip"))
                .setSaveConsumer(value -> config.renderPoppy = value)
                .build());

        return builder.build();
    }
}
