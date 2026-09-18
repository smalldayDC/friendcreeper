package com.smalldaydc.friendcreeper.client;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Vanilla-styled configuration screen, modelled on the vanilla Debug Options screen.
 * No external configuration library is required.
 */
@Environment(EnvType.CLIENT)
public class FriendCreeperConfigScreen extends Screen {

    private static final Component TITLE = Component.translatable("config.friendcreeper.title");
    private static final Component SEARCH_HINT =
            Component.translatable("config.friendcreeper.search").withStyle(EditBox.SEARCH_HINT_STYLE);
    private static final int SEARCH_BOX_WIDTH = 200;
    private static final int SEARCH_BOX_HEIGHT = 15;
    private static final Component CATEGORY_GENERAL = Component.translatable("config.friendcreeper.category.general");
    private static final Component CATEGORY_CLIENT = Component.translatable("config.friendcreeper.category.client");

    private final Screen parent;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private OptionList optionList;
    private EditBox searchBox;

    public FriendCreeperConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(4));
        header.defaultCellSetting().alignHorizontallyCenter();
        header.addChild(new StringWidget(TITLE, this.font));

        this.searchBox = header.addChild(
                new EditBox(this.font, 0, 0, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, this.searchBox, Component.empty()));
        this.searchBox.setHint(SEARCH_HINT);
        this.searchBox.setResponder(text -> {
            if (this.optionList != null) {
                this.optionList.updateSearch(text);
            }
        });
        this.layout.setHeaderHeight(12 + 9 + SEARCH_BOX_HEIGHT);

        this.optionList = this.layout.addToContents(new OptionList(this));

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
        this.optionList.updateSearch(this.searchBox.getValue());
    }

    @Override
    protected void setInitialFocus() {
        if (this.searchBox != null) {
            this.setInitialFocus(this.searchBox);
        } else {
            super.setInitialFocus();
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.optionList != null) {
            this.optionList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        FriendCreeperConfig.save();
        this.minecraft.setScreen(this.parent);
    }

    // ------------------------------------------------------------------
    // Option model
    // ------------------------------------------------------------------

    private record Option(String key, Component name, Component tooltip, boolean defaultValue,
                          Supplier<Boolean> getter, Consumer<Boolean> setter) {
    }

    private record Category(Component name, List<Option> options) {
    }

    private static Option option(String key, boolean defaultValue,
                                 Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return new Option(key,
                Component.translatable("config.friendcreeper." + key),
                Component.translatable("config.friendcreeper." + key + ".tooltip"),
                defaultValue, getter, setter);
    }

    private static List<Category> categories() {
        FriendCreeperConfig config = FriendCreeperConfig.get();
        FriendCreeperConfig defaults = new FriendCreeperConfig();

        List<Option> general = List.of(
                option("allowOwnerDamage", defaults.allowOwnerDamage,
                        () -> config.allowOwnerDamage, value -> config.allowOwnerDamage = value),
                option("followOwner", defaults.followOwner,
                        () -> config.followOwner, value -> config.followOwner = value),
                option("revengeOwner", defaults.revengeOwner,
                        () -> config.revengeOwner, value -> config.revengeOwner = value),
                option("snowGolemAttack", defaults.snowGolemAttack,
                        () -> config.snowGolemAttack, value -> config.snowGolemAttack = value),
                option("afraidOfCats", defaults.afraidOfCats,
                        () -> config.afraidOfCats, value -> config.afraidOfCats = value),
                option("naturalRegeneration", defaults.naturalRegeneration,
                        () -> config.naturalRegeneration, value -> config.naturalRegeneration = value),
                option("feedOwnerCat", defaults.feedOwnerCat,
                        () -> config.feedOwnerCat, value -> config.feedOwnerCat = value));

        List<Option> client = List.of(
                option("hurtSound", defaults.hurtSound,
                        () -> config.hurtSound, value -> config.hurtSound = value),
                option("renderPoppy", defaults.renderPoppy,
                        () -> config.renderPoppy, value -> config.renderPoppy = value),
                option("witherRoseOnLowHealth", defaults.witherRoseOnLowHealth,
                        () -> config.witherRoseOnLowHealth, value -> config.witherRoseOnLowHealth = value),
                option("tamedCreeperTexture", defaults.tamedCreeperTexture,
                        () -> config.tamedCreeperTexture, value -> config.tamedCreeperTexture = value),
                option("scaredFace", defaults.scaredFace,
                        () -> config.scaredFace, value -> config.scaredFace = value));

        return List.of(new Category(CATEGORY_GENERAL, general), new Category(CATEGORY_CLIENT, client));
    }

    // ------------------------------------------------------------------
    // List
    // ------------------------------------------------------------------

    @Environment(EnvType.CLIENT)
    static class OptionList extends ContainerObjectSelectionList<AbstractEntry> {

        private static final int ROW_WIDTH = 350;
        private static final int ITEM_HEIGHT = 20;

        private final List<Category> categories = categories();

        OptionList(FriendCreeperConfigScreen screen) {
            super(Minecraft.getInstance(), screen.width,
                    screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), ITEM_HEIGHT);
            this.updateSearch("");
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        void updateSearch(String query) {
            String needle = query.toLowerCase(Locale.ROOT).trim();
            this.clearEntries();

            for (Category category : this.categories) {
                List<Option> matched = new ArrayList<>();
                for (Option option : category.options()) {
                    if (needle.isEmpty() || matches(option, needle)) {
                        matched.add(option);
                    }
                }
                if (matched.isEmpty()) {
                    continue;
                }
                this.addEntry(new CategoryEntry(category.name()));
                for (Option option : matched) {
                    this.addEntry(new OptionEntry(option));
                }
            }

            this.setScrollAmount(0.0);
        }

        private static boolean matches(Option option, String needle) {
            return option.name().getString().toLowerCase(Locale.ROOT).contains(needle)
                    || option.key().toLowerCase(Locale.ROOT).contains(needle);
        }
    }

    // ------------------------------------------------------------------
    // Entries
    // ------------------------------------------------------------------

    @Environment(EnvType.CLIENT)
    abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
    }

    @Environment(EnvType.CLIENT)
    static class CategoryEntry extends AbstractEntry {

        private final Minecraft minecraft = Minecraft.getInstance();
        private final Component name;

        CategoryEntry(Component name) {
            this.name = name;
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawCenteredString(this.minecraft.font, this.name,
                    this.getContentX() + this.getContentWidth() / 2, this.getContentY() + 5, 0xFFFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(new NarratableEntry() {
                @Override
                public NarrationPriority narrationPriority() {
                    return NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput output) {
                    output.add(NarratedElementType.TITLE, CategoryEntry.this.name);
                }
            });
        }
    }

    @Environment(EnvType.CLIENT)
    static class OptionEntry extends AbstractEntry {

        private static final Component RESET = Component.translatable("controls.reset");
        private static final int TOGGLE_WIDTH = 75;
        private static final int RESET_WIDTH = 50;
        private static final int PADDING = 5;

        private final Minecraft minecraft = Minecraft.getInstance();
        private final Option option;
        private final CycleButton<Boolean> toggle;
        private final Button reset;
        private final List<AbstractWidget> children;
        private final List<FormattedCharSequence> tooltipLines;

        OptionEntry(Option option) {
            this.option = option;
            // Break only where the translation itself has a line break. Tooltip#splitTooltip
            // would additionally wrap at 170 px, which is not wanted here.
            this.tooltipLines = this.minecraft.font.split(option.tooltip(), Integer.MAX_VALUE);

            this.toggle = CycleButton.onOffBuilder(option.getter().get())
                    .displayOnlyValue()
                    .withCustomNarration(button -> CommonComponents.optionNameValue(option.name(), button.getMessage()))
                    .create(0, 0, TOGGLE_WIDTH, 20, option.name(), (button, value) -> {
                        option.setter().accept(value);
                        this.updateResetButton();
                    });

            this.reset = Button.builder(RESET, button -> {
                option.setter().accept(option.defaultValue());
                this.toggle.setValue(option.defaultValue());
                this.updateResetButton();
            }).width(RESET_WIDTH).build();

            this.children = List.of(this.toggle, this.reset);
            this.updateResetButton();
        }

        private void updateResetButton() {
            this.reset.active = this.option.getter().get() != this.option.defaultValue();
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = this.getContentX();
            int y = this.getContentY();
            int buttonX = x + this.getContentWidth() - RESET_WIDTH - PADDING - TOGGLE_WIDTH;

            graphics.drawString(this.minecraft.font, this.option.name(), x, y + 5, 0xFFFFFFFF);

            this.toggle.setX(buttonX);
            this.toggle.setY(y);
            this.reset.setX(this.toggle.getX() + this.toggle.getWidth() + PADDING);
            this.reset.setY(y);

            this.toggle.render(graphics, mouseX, mouseY, partialTick);
            this.reset.render(graphics, mouseX, mouseY, partialTick);

            // Only the option label shows the tooltip, not the buttons.
            if (hovered && mouseX < buttonX) {
                graphics.setTooltipForNextFrame(this.minecraft.font, this.tooltipLines, mouseX, mouseY);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
