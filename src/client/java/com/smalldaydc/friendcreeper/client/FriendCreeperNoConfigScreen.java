package com.smalldaydc.friendcreeper.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

@Environment(EnvType.CLIENT)
public class FriendCreeperNoConfigScreen extends Screen {

    private static final URI MODRINTH_URL = URI.create("https://modrinth.com/mod/cloth-config");
    private static final URI CURSEFORGE_URL = URI.create("https://www.curseforge.com/minecraft/mc-mods/cloth-config");

    private static final int LINE_HEIGHT = 15;

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

        // 先找出鼠标下方的链接，再带着下划线绘制
        URI hoveredLink = findLinkAt(mouseX, mouseY);
        visitLines(context.textRenderer(GuiGraphics.HoveredTextEffects.TOOLTIP_AND_CURSOR), hoveredLink);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            URI link = findLinkAt((int) event.x(), (int) event.y());
            if (link != null) {
                ConfirmLinkScreen.confirmLinkNow(this, link, true);
                return true;
            }
        }
        return false;
    }

    private void visitLines(ActiveTextCollector collector, @Nullable URI hoveredLink) {
        int x = this.width / 2;
        int y = this.height / 2 - 20;
        collector.accept(TextAlignment.CENTER, x, y, Component.translatable("screen.friendcreeper.noconfig.line1"));
        collector.accept(TextAlignment.CENTER, x, y + LINE_HEIGHT, Component.translatable("screen.friendcreeper.noconfig.line2"));
        collector.accept(TextAlignment.CENTER, x, y + LINE_HEIGHT * 2, Component.translatable("screen.friendcreeper.noconfig.line3",
                link("Modrinth", MODRINTH_URL, hoveredLink),
                link("CurseForge", CURSEFORGE_URL, hoveredLink)));
    }

    @Nullable
    private URI findLinkAt(int x, int y) {
        ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(this.font, x, y);
        visitLines(finder, null);
        return linkOf(finder.result());
    }

    private static Component link(String name, URI url, @Nullable URI hoveredLink) {
        return Component.literal(name).withStyle(style -> style
                .withClickEvent(new ClickEvent.OpenUrl(url))
                .withUnderlined(url.equals(hoveredLink)));
    }

    @Nullable
    private static URI linkOf(@Nullable Style style) {
        return style != null && style.getClickEvent() instanceof ClickEvent.OpenUrl(URI uri) ? uri : null;
    }
}
