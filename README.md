# MatCraft Mod (FactionCore) ⚔️

> Custom Fabric client+server mod for Minecraft 1.21.11 — combat system overhaul with blocking, HUD overlays, and 15+ mixins.
>
> Mod Fabric client+serveur sur mesure pour Minecraft 1.21.11 — refonte du système de combat avec blocage, overlays HUD et 15+ mixins.

## 🚀 Overview / Aperçu

**[EN]** FactionCore is the core mod powering the MatCraft Minecraft server experience. It overhauls the combat system with shield blocking mechanics, custom HUD overlays, combat state tracking, and extensive client-side rendering modifications via Mixin injection. Features 15+ mixins touching everything from player models to title screen branding, plus a networked blocking state system syncing combat data between server and clients.

**[FR]** FactionCore est le mod principal qui propulse l'expérience du serveur Minecraft MatCraft. Il refond le système de combat avec des mécaniques de blocage au bouclier, des overlays HUD personnalisés, du suivi d'état de combat, et des modifications extensives du rendu client via injection Mixin. Plus de 15 mixins touchant des modèles de joueurs jusqu'au branding de l'écran titre, plus un système de blocage réseau synchronisant les données de combat entre serveur et clients.

## 🛠️ Tech Stack

- **Language:** Java
- **Framework:** Fabric Mod Loader (1.21.11)
- **Mappings:** Official Mojang mappings
- **API:** Fabric API 0.141.3
- **Architecture:** Split client/server source sets (Fabric Loom)
- **Networking:** Custom payload packets (Fabric Networking v1)
- **Build:** Gradle + fabric-loom-remap

## 🧠 Technical Highlights / Défis Techniques

- **15+ Mixin injections** — deep hooks into Minecraft's internals: player models (`HumanoidModelMixin`), rendering (`LivingEntityRendererMixin`), combat (`PlayerAttackMixin`, `PlayerHurtMixin`), input (`KeyboardInputMixin`), sprint mechanics, and more
- **Split environment source sets** — clean separation between client-only code (rendering, HUD, input) and server-side logic (attack tracking, blocking state)
- **Custom networking protocol** — `BlockingStatePayload` packets syncing shield blocking state across the network in real-time
- **Combat state machine** — `CombatStateTracker` managing blocking, attacking, and hurt states with tick-based updates
- **HUD overlay system** — custom `HudOverlay` rendered via `GuiMixin` for combat information display
- **Branding injection** — `TitleScreenMixin` + `WindowBrandingMixin` for custom server identity
- **Item rendering overhaul** — `ItemInHandLayerMixin` + `ItemInHandRendererMixin` for custom weapon/shield rendering

## ✨ Features / Fonctionnalités

- ⚔️ **Combat overhaul** — custom blocking, attack tracking, sprint modifications
- 🛡️ **Shield blocking sync** — real-time networked blocking state between all players
- 🎯 **HUD overlay** — combat information displayed on-screen
- 🎨 **Custom rendering** — modified player models, item-in-hand rendering, entity rendering
- 🏷️ **Server branding** — custom title screen and window title
- 📝 **Tooltip modifications** — custom item stack tooltip rendering
- 🏃 **Sprint mechanics** — modified sprint behavior for combat balance

## 📁 Architecture

```
src/
├── main/java/qc/mat/matcraftmod/        # Server-side
│   ├── FactionCore.java                   # Main mod entrypoint
│   ├── AttackTracker.java                 # Attack event tracking
│   ├── ServerBlockingTracker.java         # Server-side block state
│   ├── mixin/
│   │   ├── PlayerAttackMixin.java         # Attack event hooks
│   │   └── PlayerHurtMixin.java           # Damage event hooks
│   └── network/
│       └── BlockingStatePayload.java      # Network packet definition
│
└── client/java/qc/mat/matcraftmod/      # Client-side
    ├── FactionCoreClient.java             # Client entrypoint
    ├── CombatStateTracker.java            # Client combat state
    ├── HudOverlay.java                    # Custom HUD rendering
    ├── BlockingPoseSettings.java          # Blocking animation config
    └── mixin/client/                      # 15 client mixins
        ├── HumanoidModelMixin.java        # Player model modifications
        ├── LivingEntityRendererMixin.java # Entity rendering hooks
        ├── ItemInHandRendererMixin.java   # Weapon/shield rendering
        ├── GuiMixin.java                  # HUD overlay injection
        ├── KeyboardInputMixin.java        # Input modifications
        ├── LocalPlayerSprintMixin.java    # Sprint behavior
        ├── TitleScreenMixin.java          # Custom title screen
        └── ...                            # + 8 more mixins
```

## 📦 Build & Deploy

```bash
# Build the mod
./gradlew build

# Output JAR
build/libs/matcraftmod-1.0.5.jar

# Install: place JAR in Minecraft mods/ directory (requires Fabric Loader)
```

## 👤 Author / Auteur

**Mathieu Fournier** — [@Maaattqc](https://github.com/Maaattqc)
