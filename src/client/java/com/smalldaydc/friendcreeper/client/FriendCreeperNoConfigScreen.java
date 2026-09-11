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

    private static final Component LINE1 = Component.translatable("screen.friendcreeper.noconfig.line1");
    private static final Component LINE2 = Component.translatable("screen.friendcreeper.noconfig.line2");
    // 第三行的三种样子：无下划线 / Modrinth 下划线 / CurseForge 下划线
    private static final Component LINE3 = buildLine3(null);
    private static final Component LINE3_MODRINTH = buildLine3(MODRINTH_URL);
    private static final Component LINE3_CURSEFORGE = buildLine3(CURSEFORGE_URL);
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

        URI hovered = findLinkAt(mouseX, mouseY);
        Component line3 = MODRINTH_URL.equals(hovered) ? LINE3_MODRINTH
                : CURSEFORGE_URL.equals(hovered) ? LINE3_CURSEFORGE
                : LINE3;
        visitLines(context.textRenderer(GuiGraphics.HoveredTextEffects.TOOLTIP_AND_CURSOR), line3);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        URI link = event.button() == InputConstants.MOUSE_BUTTON_LEFT ? findLinkAt((int) event.x(), (int) event.y()) : null;
        if (link == null) {
            return false;
        }
        ConfirmLinkScreen.confirmLinkNow(this, link, true);
        return true;
    }

    private void visitLines(ActiveTextCollector collector, Component line3) {
        int x = this.width / 2;
        int y = this.height / 2 - 20;
        collector.accept(TextAlignment.CENTER, x, y, LINE1);
        collector.accept(TextAlignment.CENTER, x, y + LINE_HEIGHT, LINE2);
        collector.accept(TextAlignment.CENTER, x, y + LINE_HEIGHT * 2, line3);
    }

    @Nullable
    private URI findLinkAt(int x, int y) {
        ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(this.font, x, y);
        visitLines(finder, LINE3);
        return finder.result() != null && finder.result().getClickEvent() instanceof ClickEvent.OpenUrl(URI uri) ? uri : null;
    }

    private static Component buildLine3(@Nullable URI hoveredLink) {
        return Component.translatable("screen.friendcreeper.noconfig.line3",
                link("Modrinth", MODRINTH_URL, hoveredLink),
                link("CurseForge", CURSEFORGE_URL, hoveredLink));
    }

    private static Component link(String name, URI url, @Nullable URI hoveredLink) {
        return Component.literal(name).withStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.OpenUrl(url))
                .withUnderlined(url.equals(hoveredLink)));
    }
}
