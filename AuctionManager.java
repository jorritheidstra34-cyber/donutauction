package nl.donutauction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Houdt de veiling bij: leest chatberichten, onthoudt de hoogste enkele betaling
 * en beheert de timer. Bevat geen Minecraft-specifieke code, dus makkelijk te testen.
 */
public final class AuctionManager {

    /**
     * Herkent berichten als "Naam paid you $1,500" of "Naam paid you $1.5K".
     * Past de tekst aan als DonutSMP het formaat verandert.
     */
    private static final Pattern PAID_YOU =
            Pattern.compile("(?i)^\\s*(\\w{3,16}) paid you \\$([0-9][0-9.,]*)\\s*([kmbt]?)");

    public static final int[] DURATIONS_SECONDS = {30, 60, 90, 120, 180, 300};

    private static int durationIndex = 1;
    private static boolean running = false;
    private static long endTimeMs = 0;
    private static String bestName = "-";
    private static double bestAmount = 0;
    private static int bidCount = 0;

    private AuctionManager() {}

    public static boolean isRunning() { return running; }
    public static String bestName() { return bestName; }
    public static double bestAmount() { return bestAmount; }
    public static int bidCount() { return bidCount; }
    public static int durationSeconds() { return DURATIONS_SECONDS[durationIndex]; }

    public static long remainingSeconds() {
        if (!running) return durationSeconds();
        return Math.max(0, (endTimeMs - System.currentTimeMillis() + 999) / 1000);
    }

    public static void cycleDuration() {
        if (running) return; // niet tijdens een veiling wisselen
        durationIndex = (durationIndex + 1) % DURATIONS_SECONDS.length;
    }

    public static void start() {
        running = true;
        endTimeMs = System.currentTimeMillis() + durationSeconds() * 1000L;
        bestName = "-";
        bestAmount = 0;
        bidCount = 0;
    }

    /** Stopt de veiling. Geeft true terug als er echt een veiling liep. */
    public static boolean stop() {
        boolean was = running;
        running = false;
        return was;
    }

    /** Elke client-tick aanroepen. Geeft true terug op het moment dat de tijd om is. */
    public static boolean tickAndCheckFinished() {
        if (running && System.currentTimeMillis() >= endTimeMs) {
            running = false;
            return true;
        }
        return false;
    }

    /** Verwerk een binnenkomend chatbericht (platte tekst). */
    public static void onChat(String text) {
        if (!running) return;
        Matcher m = PAID_YOU.matcher(text);
        if (!m.find()) return;

        double amount = parseAmount(m.group(2), m.group(3));
        if (amount <= 0) return;

        bidCount++;
        if (amount > bestAmount) {
            bestAmount = amount;
            bestName = m.group(1);
        }
    }

    static double parseAmount(String number, String suffix) {
        try {
            double value = Double.parseDouble(number.replace(",", ""));
            return switch (suffix.toLowerCase()) {
                case "k" -> value * 1_000d;
                case "m" -> value * 1_000_000d;
                case "b" -> value * 1_000_000_000d;
                case "t" -> value * 1_000_000_000_000d;
                default -> value;
            };
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 1500 -> "$1.5K", 2_000_000 -> "$2M" */
    public static String format(double amount) {
        String[] units = {"", "K", "M", "B", "T"};
        int i = 0;
        double v = amount;
        while (v >= 1000 && i < units.length - 1) {
            v /= 1000;
            i++;
        }
        String s = (v == Math.floor(v)) ? String.valueOf((long) v) : String.format("%.2f", v);
        return "$" + s + units[i];
    }
}
