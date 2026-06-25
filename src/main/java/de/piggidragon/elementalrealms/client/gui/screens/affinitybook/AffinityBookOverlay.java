package de.piggidragon.elementalrealms.client.gui.screens.affinitybook;

import de.piggidragon.elementalrealms.ElementalRealms;
import de.piggidragon.elementalrealms.magic.affinities.Affinity;
import de.piggidragon.elementalrealms.registries.attachments.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Overlay panel rendered next to the inventory; lists the player's affinities and completion.
 */
public class AffinityBookOverlay {

    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(ElementalRealms.MODID, "textures/gui/affinity_book.png");

    private static final int WIDTH = 147;
    private static final int HEIGHT = 166;
    private static final int COMPLETION_FULL = 100;
    private static final int COLOR_THRESHOLD_MID = 50;
    private static final int COLOR_RED = 255;
    private static final int COLOR_GREEN_HALF = 0x88;
    private static final int ALPHA_MASK = 0xFF000000;
    private static final int TEXT_COLOR_WHITE = 0xFFFFFF;
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_SPACING = 6;
    private static final int TEXT_X_OFFSET = 10;
    private static final int TITLE_Y_OFFSET = 10;
    private static final int FIRST_GROUP_Y_OFFSET = 30;
    private static final int PERCENT_X_OFFSET = 87;

    private final Player player;
    private final Minecraft minecraft;
    private final Font font;
    private boolean visible = false;

    public AffinityBookOverlay(Player player) {
        this.player = player;
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
    }

    public void toggleVisibility() {
        this.visible = !this.visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public void render(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        graphics.blit(BACKGROUND, x - 2, y, 1, 1, WIDTH, HEIGHT, 256, 256);

        Component title = Component.translatable("affinity_book.elementalrealms.title");
        int titleX = x + (WIDTH - this.font.width(title)) / 2 - 2;
        graphics.drawString(this.font, title, titleX, y + TITLE_Y_OFFSET, TEXT_COLOR_WHITE, false);

        renderAffinities(graphics, x, y, this.player);
    }

    private void renderAffinities(GuiGraphics graphics, int baseX, int baseY, Player player) {
        int startY = baseY + FIRST_GROUP_Y_OFFSET;
        int xOffset = baseX + TEXT_X_OFFSET;

        Map<Affinity, Integer> completionMap = player.getData(ModAttachments.AFFINITIES.get());

        Map<AffinityType, List<Affinity>> groups = new EnumMap<>(AffinityType.class);
        for (AffinityType type : AffinityType.values()) {
            groups.put(type, new ArrayList<>());
        }
        for (Affinity affinity : completionMap.keySet()) {
            if (affinity == Affinity.VOID) continue;
            groups.get(affinity.getType()).add(affinity);
        }
        groups.values().forEach(list -> list.sort(Comparator.comparingInt(completionMap::get).reversed()));

        startY = renderAffinityGroup(graphics, xOffset, startY, groups.get(AffinityType.ELEMENTAL), completionMap);
        startY += ROW_SPACING;
        startY = renderAffinityGroup(graphics, xOffset, startY, groups.get(AffinityType.DEVIANT), completionMap);
        startY += ROW_SPACING;
        renderAffinityGroup(graphics, xOffset, startY, groups.get(AffinityType.ETERNAL), completionMap);
    }

    private int renderAffinityGroup(
            GuiGraphics graphics,
            int x,
            int y,
            List<Affinity> group,
            Map<Affinity, Integer> map
    ) {
        for (Affinity affinity : group) {
            int completion = map.getOrDefault(affinity, 0);
            boolean isCompleted = completion >= COMPLETION_FULL;
            renderAffinityEntry(graphics, affinity, completion, isCompleted, x, y);
            y += ROW_HEIGHT;
        }
        return y;
    }

    private void renderAffinityEntry(
            GuiGraphics graphics,
            Affinity affinity,
            int completion,
            boolean isCompleted,
            int x,
            int y
    ) {
        Component name = Component.translatable("affinity.elementalrealms." + affinity.getName());
        graphics.drawString(this.font, name, x, y, getAffinityColor(affinity), isCompleted);

        String percentText = completion + "%";
        graphics.drawString(this.font, percentText, x + PERCENT_X_OFFSET, y, getProgressColor(completion), false);
    }

    /**
     * 0% = red, 50% = orange, 100% = green. Linear interpolation between the stops.
     */
    private int getProgressColor(int percent) {
        percent = Math.clamp(percent, 0, COMPLETION_FULL);
        if (percent < COLOR_THRESHOLD_MID) {
            float ratio = (float) percent / COLOR_THRESHOLD_MID;
            return ALPHA_MASK | (COLOR_RED << 16) | ((int) (COLOR_GREEN_HALF * ratio) << 8);
        }
        float ratio = (float) (percent - COLOR_THRESHOLD_MID) / COLOR_THRESHOLD_MID;
        int red = (int) (COLOR_RED * (1 - ratio));
        int green = (int) (COLOR_GREEN_HALF + (COLOR_RED - COLOR_GREEN_HALF) * ratio);
        return ALPHA_MASK | (red << 16) | (green << 8);
    }

    private int getAffinityColor(Affinity affinity) {
        return switch (affinity) {
            case VOID -> 0xFF000000;
            case FIRE -> 0xFFFF4500;
            case WATER -> 0xFF1E90FF;
            case EARTH -> 0xFF8B4513;
            case WIND -> 0xFFE0FFFF;
            case LIGHTNING -> 0xFFFFFF00;
            case ICE -> 0xFF87CEEB;
            case SOUND -> 0xFFDA70D6;
            case GRAVITY -> 0xFF4B0082;
            case TIME -> 0xFFFFD700;
            case SPACE -> 0xFF191970;
            case LIFE -> 0xFF32CD32;
        };
    }

    private enum AffinityType {
        ELEMENTAL, DEVIANT, ETERNAL
    }
}
