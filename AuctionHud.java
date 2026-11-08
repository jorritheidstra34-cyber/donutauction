package nl.donutauction;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Tekent het veilingpaneel linksboven. Pas X/Y aan om het te verplaatsen. */
public final class AuctionHud {

    private static final int X = 6;
    private static final int Y = 6;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 11;

    private static final int COLOR_BG = 0xAA000000;
    private static final int COLOR_TITLE = 0xFFFFAA00;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_DIM = 0xFFAAAAAA;
    private static final int COLOR_GOOD = 0xFF55FF55;

    private AuctionHud() {}

    // 26.x-API: GuiGraphics kan in nieuwere versies anders heten
    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        Font font = mc.font;
        boolean running = AuctionManager.isRunning();

        String title = running ? "VEILING LOOPT" : "Veiling (J = start)";
        String time = "Tijd: " + AuctionManager.remainingSeconds() + "s";
        String top = "Hoogste: " + (AuctionManager.bidCount() == 0
                ? "-" : AuctionManager.bestName() + " " + AuctionManager.format(AuctionManager.bestAmount()));
        String bids = "Biedingen: " + AuctionManager.bidCount();

        int width = 0;
        for (String s : new String[]{title, time, top, bids}) {
            width = Math.max(width, font.width(s));
        }
        width += PADDING * 2;
        int height = LINE_HEIGHT * 4 + PADDING * 2 - 2;

        g.fill(X, Y, X + width, Y + height, COLOR_BG);

        int x = X + PADDING;
        int y = Y + PADDING;
        g.drawString(font, title, x, y, running ? COLOR_TITLE : COLOR_DIM, true);
        g.drawString(font, time, x, y + LINE_HEIGHT, COLOR_TEXT, true);
        g.drawString(font, top, x, y + LINE_HEIGHT * 2, COLOR_GOOD, true);
        g.drawString(font, bids, x, y + LINE_HEIGHT * 3, COLOR_DIM, true);
    }
}
