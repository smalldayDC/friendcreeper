package com.smalldaydc.friendlycreeper.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FriendlyCreeperNoConfigScreen extends Screen {

    private final Screen parent;

    public FriendlyCreeperNoConfigScreen(Screen parent) {
        super(Text.literal("Friend Creeper Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> this.client.setScreen(parent))
                .dimensions(this.width / 2 - 75, this.height / 2 + 40, 150, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Cloth Config API is not installed"),
                this.width / 2, this.height / 2 - 30, 0xFF5555);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("You don't have Cloth Config API installed;"),
                this.width / 2, this.height / 2 - 10, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("please install it before configuring via the graphical interface."),
                this.width / 2, this.height / 2 + 5, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("You can download it from Modrinth or CurseForge."),
                this.width / 2, this.height / 2 + 20, 0xAAAAAA);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderDarkening(context);
    }
}
