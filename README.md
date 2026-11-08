# Donut Auction (Fabric, Minecraft 26.2)

Client-side mod voor DonutSMP: start een veiling, de mod leest "X paid you $..."
in de chat en toont de hoogste betaling in een HUD.

## Gebruik in-game
- **J**: veiling starten / stoppen
- **K**: veilingduur wisselen (30 / 60 / 90 / 120 / 180 / 300 sec), alleen als er geen veiling loopt
- Na afloop zie je in je eigen chat wie won. Er wordt niets naar de server gestuurd.
- Wijzigen kan via Opties > Besturing > Donut Auction.

## Bouwen
1. Installeer **JDK 25** (Temurin).
2. Ga naar https://fabricmc.net/develop, kies Minecraft **26.2** en zet de
   `loader_version` en `fabric_version` in `gradle.properties`.
3. Haal de Gradle wrapper op, of kopieer die uit de Fabric example mod
   (https://github.com/FabricMC/fabric-example-mod), en voer uit:

       ./gradlew build        (Windows: gradlew.bat build)

4. De mod staat dan in `build/libs/donutauction-1.0.0.jar`. Zet die samen met
   Fabric API in je `mods`-map.

## Als het niet compileert
Alleen deze plekken zijn versiegevoelig (gemarkeerd met `26.x-API`):
- `DonutAuctionClient`: `KeyMapping.Category`, `KeyMappingHelper`, `HudElementRegistry`
- `AuctionHud`: `GuiGraphics` en `drawString`

Zie https://docs.fabricmc.net/develop/porting/ voor de actuele namen.

## Betaalbericht aanpassen
Als DonutSMP het formaat verandert, pas dan `PAID_YOU` aan in `AuctionManager.java`.
