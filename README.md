# MatCraft Mod — FactionCore

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Fabric](https://img.shields.io/badge/Fabric_Loader-0.18.4-blue?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKM+VkTtIA0EQhr+NihpFwcJCLCIWRjGKYKMYNIIWYtQi3jYXE4W9W3YTIdiKtYWFaOGr8A9Qa2GhFkEFETtxaxXj+E4hEIwDw3x8M/wMA9YkY1X0TA+y+byOxqMRaW5esvxg4Y2OmlWGrmJq+v+3LT/u0bK+D5h6VfPjruoAfkN5AJXxPgBuJTzJAvoAS4DV3J+k5o/GI0+puUPRqPKg2cpaIlHE5YWsvmCYhzucT/GrVjKchH4VNX2WcICsCWkAg1fRygkAv/MUExOh6dw6HJogT3gAfYAT4Bv6Z4Aw9wEAAAAASUVORK5CYII=)
![Minecraft 1.21.11](https://img.shields.io/badge/Minecraft-1.21.11-green?logo=mojangstudios&logoColor=white)
![Mixins: 17](https://img.shields.io/badge/Mixins-17-purple)
![License: CC0](https://img.shields.io/badge/License-CC0_1.0-lightgrey)

**Client+server Fabric mod that overhauls Minecraft's combat system — sword blocking with networked sync, 1.7-style animations, anti-cheat measures, custom HUD, and 17 Mixin injections into Minecraft's internals.**

*Mod Fabric client+serveur qui refond le système de combat de Minecraft — blocage à l'épée avec synchronisation réseau, animations style 1.7, mesures anti-triche, HUD personnalisé et 17 injections Mixin dans les entrailles du jeu.*

---

## Table of Contents / Table des matières

- [Overview](#overview--aperçu)
- [Architecture](#architecture)
- [Design Patterns & Technical Depth](#design-patterns--technical-depth)
- [Mixin Catalog](#mixin-catalog--catalogue-des-mixins)
- [Features](#features--fonctionnalités)
- [Tech Stack](#tech-stack--stack-technique)
- [Build & Install](#build--install)
- [Author](#author--auteur)

---

## Overview / Aperçu

**[EN]** FactionCore is a comprehensive combat overhaul mod for Minecraft 1.21.11 that brings back the beloved pre-1.9 sword-blocking mechanic while adding modern networked synchronization. The mod operates on both client and server, using Fabric's split source set architecture to cleanly separate rendering code from game logic. On the client side, 15 Mixin injections modify everything from player model poses to first-person item rendering to input handling. On the server side, a custom networking protocol broadcasts blocking states to nearby players in real-time, while anti-cheat systems enforce CPS caps and reach limits.

**[FR]** FactionCore est un mod de refonte complète du combat pour Minecraft 1.21.11 qui ramène la mécanique de blocage à l'épée d'avant 1.9 tout en ajoutant une synchronisation réseau moderne. Le mod opère côté client et serveur, utilisant l'architecture de source sets séparés de Fabric pour isoler proprement le code de rendu de la logique de jeu. Côté client, 15 injections Mixin modifient tout, des poses du modèle joueur au rendu d'items en première personne en passant par la gestion des entrées. Côté serveur, un protocole réseau custom diffuse les états de blocage aux joueurs proches en temps réel, tandis que des systèmes anti-triche imposent des limites de CPS et de portée.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                          FactionCore (Server)                        │
│           ModInitializer — Payload registration, event hooks         │
│   ┌──────────────┐  ┌─────────────────┐  ┌──────────────────────┐   │
│   │AttackTracker  │  │ServerBlocking   │  │BlockingStatePayload  │   │
│   │CPS cap (10/s) │  │Tracker (Set)    │  │Custom network packet │   │
│   └───────┬───────┘  └────────┬────────┘  └──────────┬───────────┘   │
│           │                   │                      │               │
│   ┌───────▼───────┐  ┌───────▼────────┐             │               │
│   │PlayerAttack   │  │PlayerHurt      │             │               │
│   │Mixin (CPS)    │  │Mixin (dmg red.)│             │               │
│   └───────────────┘  └────────────────┘             │               │
└──────────────────────────────────────────────────────┼───────────────┘
                          ▲ C2S packets                │ S2C broadcast
                          │                            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                       FactionCoreClient (Client)                     │
│              ClientModInitializer — Tick events, receivers           │
│                                                                      │
│   ┌───────────────────┐  ┌────────────────┐  ┌──────────────────┐   │
│   │CombatStateTracker │  │HudOverlay      │  │BlockingPose      │   │
│   │GLFW raw input     │  │FPS/CPS/Reach   │  │Settings (tuning) │   │
│   │Blocking detection │  │Tick-cached text │  │Rotation, offsets │   │
│   └─────────┬─────────┘  └───────┬────────┘  └──────────────────┘   │
│             │                    │                                    │
│   ┌─────────▼────────────────────▼──────────────────────────────┐   │
│   │                    15 Client Mixins                           │   │
│   │                                                              │   │
│   │  Rendering          Combat            Input & UI             │   │
│   │  ─────────          ──────            ──────────             │   │
│   │  HumanoidModel      EntitySprint      KeyboardInput          │   │
│   │  HumanoidRender      LocalPlayerSprint MinecraftMixin         │   │
│   │    State             LivingEntity      TitleScreen            │   │
│   │  ItemInHandLayer       Swing           WindowBranding         │   │
│   │  ItemInHandRenderer  MultiPlayerGame   ItemStackTooltip       │   │
│   │  LivingEntityRenderer  Mode                                  │   │
│   │  GuiMixin                                                    │   │
│   └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### Source Layout

```
src/
├── main/java/qc/mat/matcraftmod/              # Server-side (both environments)
│   ├── FactionCore.java                        # Entry: payload registration, attribute mods
│   ├── AttackTracker.java                      # Per-player CPS rate limiter
│   ├── ServerBlockingTracker.java              # UUID set of blocking players
│   ├── mixin/
│   │   ├── PlayerAttackMixin.java              # Server-side CPS enforcement
│   │   └── PlayerHurtMixin.java                # Directional damage reduction when blocking
│   └── network/
│       └── BlockingStatePayload.java           # StreamCodec-based custom packet
│
├── client/java/qc/mat/matcraftmod/            # Client-side only
│   ├── FactionCoreClient.java                  # Client entry: tick events, receivers
│   ├── CombatStateTracker.java                 # Blocking state from raw GLFW input
│   ├── HudOverlay.java                         # FPS / CPS / Reach overlay
│   ├── BlockingPoseSettings.java               # Tunable animation constants
│   ├── BlockingStateAccess.java                # Interface injected via Mixin
│   └── mixin/client/
│       ├── HumanoidModelMixin.java             # Arm pose + swing curve override
│       ├── HumanoidRenderStateMixin.java       # Blocking state injection into render state
│       ├── ItemInHandLayerMixin.java           # 3rd-person sword blocking transform
│       ├── ItemInHandRendererMixin.java        # 1st-person blocking pose (1.7 style)
│       ├── LivingEntityRendererMixin.java      # Blocking state extraction per entity
│       ├── LivingEntitySwingMixin.java         # Swing duration clamp (≤4 ticks)
│       ├── GuiMixin.java                       # HUD overlay injection + crosshair fix
│       ├── EntitySprintMixin.java              # Prevent sprint while blocking
│       ├── LocalPlayerSprintMixin.java         # Sprint input suppression
│       ├── KeyboardInputMixin.java             # Movement slow (0.3×) while blocking
│       ├── MinecraftMixin.java                 # Attack prevention + eat-and-mine
│       ├── MultiPlayerGameModeMixin.java       # Reach validation + CPS cap (client)
│       ├── ItemStackTooltipMixin.java          # Hide attack speed, spoof namespace
│       ├── TitleScreenMixin.java               # Remove copyright text
│       └── WindowBrandingMixin.java            # Custom title + icon (STBImage)
│
└── client/resources/
    └── matcraftmod.client.mixins.json          # 15 client mixin declarations
```

---

## Design Patterns & Technical Depth

### Mixin Injection Strategy

The mod uses **17 Mixin injections** across 2 environments (server + client). Techniques include:
- **`@Inject` at HEAD** — canceling vanilla behavior (attack prevention, sprint suppression)
- **`@Inject` at TAIL** — appending behavior (HUD rendering, movement scaling)
- **`@Redirect`** — replacing individual method calls without touching the rest (crosshair indicator removal, swing easing curve replacement)
- **`@ModifyVariable`** — surgically modifying a method argument in-place (damage reduction on blocking)
- **Interface injection** — `BlockingStateAccess` injected into `HumanoidRenderState` via `@Implements`-style Mixin to carry blocking data through Minecraft's render pipeline

### Client-Server Sync Protocol

Blocking state is synced via a **custom `BlockingStatePayload`** packet (Fabric Networking v1):
1. Client detects right-click held + weapon in hand via **raw GLFW input** (bypassing Minecraft's `KeyMapping` which doesn't register in air)
2. Client sends C2S packet with `(entityId=0, blocking=true/false)`
3. Server validates, updates `ServerBlockingTracker`, applies sprint cancellation
4. Server broadcasts S2C `(entityId=sender.getId(), blocking)` to all players within 64 blocks
5. Receiving clients update `CombatStateTracker.remoteBlockingEntities` for correct 3rd-person rendering

### 1.7-Style Combat Recreation

The mod recreates pre-1.9 Minecraft combat through multiple coordinated systems:
- **No attack cooldown** — attribute modifier adds +1020 attack speed on join, effectively removing the cooldown bar
- **Sword blocking** — right-click with any tool triggers blocking state (50% frontal damage reduction via dot-product direction check)
- **Classic swing curve** — `outQuart` easing replaced with `Mth.sqrt()` for instant, snappy swings
- **Swing duration cap** — clamped to 4 ticks max via `getCurrentSwingDuration` override
- **Movement slow while blocking** — input vector scaled to 0.3× in `KeyboardInputMixin`
- **Sprint prevention** — three-layer enforcement (Entity, LocalPlayer AI step, CombatStateTracker)

### Anti-Cheat Measures

Both client and server enforce fair play:
- **Server-side CPS cap** — `AttackTracker` enforces 100ms minimum between attacks (10 CPS max) with `ConcurrentHashMap<UUID, Long>`
- **Client-side CPS cap** — `MultiPlayerGameModeMixin` mirrors the same 100ms restriction
- **Reach validation** — client-side calculates eye-to-AABB distance and cancels attacks beyond 3.03 blocks

### First-Person Blocking Renderer

`ItemInHandRendererMixin` completely **overrides** the vanilla `renderArmWithItem` method when blocking:
- Applies vanilla BLOCK transform constants
- Adds configurable tilt, height offset, and depth
- Implements swing-while-blocking with pitch/yaw rotation driven by `sin(pow(swingProgress, 0.65))` for a natural feel
- Handles eat-while-mining by intercepting `handleKeybinds` to manually process attack inputs during item use

### HUD Overlay Architecture

`HudOverlay` separates **tick-rate logic** (20 Hz) from **render-rate display** (variable FPS):
- Click tracking uses an `ArrayDeque<Long>` with 1-second sliding window
- Cached strings updated once per tick, rendered every frame — zero allocation during render
- Reach display updated from `MultiPlayerGameModeMixin` on each attack

### Branding System

`WindowBrandingMixin` sets a custom window title and loads a **custom icon via STBImage** — reading PNG bytes from mod resources, decoding with `stbi_load_from_memory`, and setting via `glfwSetWindowIcon`. Includes proper memory management with manual buffer allocation and `stbi_image_free`.

---

## Mixin Catalog / Catalogue des Mixins

| # | Mixin | Target | Technique | Purpose |
|---|---|---|---|---|
| 1 | `PlayerAttackMixin` | `Player.attack()` | `@Inject HEAD` | Server CPS rate limiting |
| 2 | `PlayerHurtMixin` | `Player.hurtServer()` | `@ModifyVariable` | 50% damage reduction when blocking (frontal) |
| 3 | `HumanoidModelMixin` | `HumanoidModel` | `@Redirect` + `@Inject` | Classic swing curve + blocking arm pose |
| 4 | `HumanoidRenderStateMixin` | `HumanoidRenderState` | Interface injection | Carries blocking state through render pipeline |
| 5 | `ItemInHandLayerMixin` | `ItemInHandLayer` | `@Inject` | 3rd-person sword blocking transform |
| 6 | `ItemInHandRendererMixin` | `ItemInHandRenderer` | `@Inject HEAD` (cancel) | Full 1st-person blocking pose override |
| 7 | `LivingEntityRendererMixin` | `LivingEntityRenderer` | `@Inject HEAD` | Extracts blocking state per entity for rendering |
| 8 | `LivingEntitySwingMixin` | `LivingEntity` | `@Inject RETURN` | Clamps swing duration to 4 ticks |
| 9 | `GuiMixin` | `Gui` | `@Redirect` × 2 + `@Inject` | Hides cooldown indicators + renders HUD overlay |
| 10 | `EntitySprintMixin` | `Entity.setSprinting()` | `@Inject HEAD` | Cancels sprint activation while blocking |
| 11 | `LocalPlayerSprintMixin` | `LocalPlayer.aiStep()` | `@Inject` × 2 | Suppresses sprint input + forces sprint off |
| 12 | `KeyboardInputMixin` | `KeyboardInput.tick()` | `@Inject TAIL` | 0.3× movement speed while blocking |
| 13 | `MinecraftMixin` | `Minecraft` | `@Inject` × 2 | Prevents attacks while blocking + eat-and-mine |
| 14 | `MultiPlayerGameModeMixin` | `MultiPlayerGameMode` | `@Inject HEAD` | Client CPS cap + reach validation (3.03 blocks) |
| 15 | `ItemStackTooltipMixin` | `ItemStack` | `@Inject` × 2 | Hides attack speed tooltip + spoofs mod namespace |
| 16 | `TitleScreenMixin` | `TitleScreen.init()` | `@Inject TAIL` | Removes copyright text from title screen |
| 17 | `WindowBrandingMixin` | `Minecraft.updateTitle()` | `@Inject TAIL` | Custom window title + icon via STBImage |

---

## Features / Fonctionnalites

| Feature | Description EN | Description FR |
|---|---|---|
| **Sword Blocking** | Right-click with any tool to block — 50% frontal damage reduction with directional dot-product check | Clic droit avec n'importe quel outil pour bloquer — réduction de 50% des dégâts frontaux via produit scalaire directionnel |
| **Networked Sync** | Blocking state broadcast to all nearby players via custom packets — correct 3rd-person pose for everyone | État de blocage diffusé à tous les joueurs proches via paquets custom — pose 3e personne correcte pour tous |
| **No Cooldown** | Attack speed modifier removes 1.9+ cooldown, recreating classic 1.7 PvP feel | Modificateur de vitesse d'attaque supprime le cooldown 1.9+, recréant le PvP classique 1.7 |
| **Classic Animations** | `sqrt` swing curve, 4-tick swing cap, first-person blocking pose matching 1.7.10 | Courbe de swing `sqrt`, swing plafonné à 4 ticks, pose de blocage 1re personne fidèle à 1.7.10 |
| **Anti-Cheat** | CPS cap (10/s) on both client + server, reach limit (3.03 blocks) | Limite CPS (10/s) client + serveur, limite de portée (3.03 blocs) |
| **Combat HUD** | FPS, CPS, and reach distance overlay with tick-cached rendering | Overlay FPS, CPS et distance de portée avec rendu cache-par-tick |
| **Eat & Mine** | Break blocks while eating — manually routes attack inputs during item use | Casser des blocs en mangeant — routage manuel des inputs d'attaque pendant l'utilisation d'objets |
| **Branding** | Custom window title, icon, and copyright removal for server identity | Titre de fenêtre, icône custom et suppression du copyright pour l'identité serveur |

---

## Tech Stack / Stack technique

| Component | Technology |
|---|---|
| Language | Java 21 |
| Mod Loader | Fabric Loader 0.18.4 |
| Minecraft | 1.21.11 |
| API | Fabric API 0.141.3 |
| Mappings | Official Mojang |
| Build System | Gradle + fabric-loom-remap |
| Networking | Fabric Networking v1, StreamCodec, custom payloads |
| Rendering | Blaze3D PoseStack, LWJGL 3 (GLFW, STBImage) |
| Injection | SpongePowered Mixin 0.8+ (17 mixins) |
| Concurrency | ConcurrentHashMap for server-side state tracking |

---

## Build & Install

```bash
# Clone
git clone https://github.com/Maaattqc/matcraft-mod.git
cd matcraft-mod

# Build
./gradlew build

# Output JAR → build/libs/matcraftmod-1.0.5.jar
# Install in .minecraft/mods/ (client) AND server mods/ (server)
# Requires Fabric Loader 0.18.4+ and Fabric API
```

**Requirements:** Java 21, Fabric Loader 0.18.4+, Fabric API — both client and server

---

## Author / Auteur

**Mathieu Fournier** — [@Maaattqc](https://github.com/Maaattqc)
