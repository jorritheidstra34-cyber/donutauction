package nl.donutauction;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Entrypoint. Let op: de plekken met "26.x-API" zijn de enige stukken die kunnen
 * afwijken per Minecraft/Fabric API-versie. Geeft Gradle daar een foutmelding,
 * dan hoef je alleen de naam aan te passen (zie README).
 */
public class DonutAuctionClient implements ClientModInitializer {

    public static final String MOD_ID = "donutauction";

    private static KeyMapping toggleKey;
    private static KeyMapping durationKey;

    @Override
    public void onInitializeClient() {
        // 26.x-API: key mappings met een eigen categorie
        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.donutauction.toggle", InputConstants.KEY_J, category));
        durationKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.donutauction.duration", InputConstants.KEY_K, category));

        // Chat meelezen (geen overlay/actionbar)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                AuctionManager.onChat(message.getString());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        // 26.x-API: HUD-element vóór de chat tekenen
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "auction_hud"),
                AuctionHud::render);
    }

    private void onTick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            if (AuctionManager.isRunning()) {
                AuctionManager.stop();
                say(mc, "Veiling gestopt.");
            } else {
                AuctionManager.start();
                say(mc, "Veiling gestart voor " + AuctionManager.durationSeconds() + " seconden!");
            }
        }

        while (durationKey.consumeClick()) {
            if (!AuctionManager.isRunning()) {
                AuctionManager.cycleDuration();
                say(mc, "Veilingduur: " + AuctionManager.durationSeconds() + " seconden");
            }
        }

        if (AuctionManager.tickAndCheckFinished()) {
            if (AuctionManager.bidCount() == 0) {
                say(mc, "Veiling afgelopen. Geen biedingen.");
            } else {
                say(mc, "Veiling afgelopen! Hoogste: " + AuctionManager.bestName()
                        + " met " + AuctionManager.format(AuctionManager.bestAmount()));
            }
        }
    }

    /** Toont een bericht alleen voor jezelf (wordt niet naar de server gestuurd). */
    private static void say(Minecraft mc, String text) {
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(Component.literal("[Auction] " + text));
        }
    }
}
