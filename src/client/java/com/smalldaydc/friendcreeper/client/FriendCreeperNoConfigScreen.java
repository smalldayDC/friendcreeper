package com.smalldaydc.friendcreeper.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FriendCreeperNoConfigScreen extends Screen {

    private static final Component[] MESSAGES = {
        Component.translatable("screen.friendcreeper.noconfig.line1"),
        Component.translatable("screen.friendcreeper.noconfig.line2"),
        Component.translatable("screen.friendcreeper.noconfig.line3")
    };

    private final Screen parent;

    public FriendCreeperNoConfigScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.friendcreeper.noconfig.back"),
                button -> this.minecraft.setScreen(parent))
                .bounds(this.width / 2 - 75, this.height / 2 + 40, 150, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (int i = 0; i < MESSAGES.length; i++) {
            context.drawCenteredString(this.font,
                    MESSAGES[i], this.width / 2, this.height / 2 - 20 + i * 15, 0xFFFFFFFF);
        }
    }
}
