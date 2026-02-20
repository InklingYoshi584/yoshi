# Yoshi Mod - Abilities Implementation Plan

## [x] Task 1: Set up Keybindings

* **Priority**: P0

* **Depends On**: None

* **Description**:

  * Register keybind for reel-in ability

  * Configure keybind in client setup

* **Success Criteria**:

  * Keybind is registered and accessible in controls menu

  * Keybind triggers appropriate event when pressed

* **Test Requirements**:

  * `programmatic` TR-1.1: Keybind is registered without errors

  * `human-judgement` TR-1.2: Keybind appears in controls menu and can be rebound

## [x] Task 2: Implement Flutter Jump Ability

* **Priority**: P1

* **Depends On**: None

* **Description**:

  * Detect when player holds spacebar in midair

  * Implement flutter jump mechanics (using jump state animation)

  * Add velocity boost for flutter effect

* **Success Criteria**:

  * Player can hold spacebar in midair to perform flutter jump

  * Animation uses existing jump state

  * Flutter jump provides additional lift

* **Test Requirements**:

  * `programmatic` TR-2.1: Flutter jump activates only when spacebar is held in midair

  * `human-judgement` TR-2.2: Flutter jump provides noticeable additional height

## [x] Task 3: Implement Entity Detection System

* **Priority**: P1

* **Depends On**: None

* **Description**:

  * Create system to detect entities within 3 block radius

  * Implement 10 degree angle of view check

  * Prioritize closest/most relevant entity

* **Success Criteria**:

  * System correctly identifies entities within range and angle

  * Only valid entities are considered (excludes players, items, etc.)

* **Test Requirements**:

  * `programmatic` TR-3.1: Entity detection works within specified radius and angle

  * `human-judgement` TR-3.2: Detection feels responsive and intuitive

## [x] Task 4: Implement Reel-in Mechanics

* **Priority**: P1

* **Depends On**: Task 1, Task 3

* **Description**:

  * Create smooth reel-in animation/movement for entities(Can just use set vector motion

  * Handle collision and movement during reel-in

  * Determine when entity is close enough to player(when touching)

* **Success Criteria**:

  * Entities are smoothly pulled toward player

  * Reel-in stops when entity reaches player

  * No teleportation or jerky movements

* **Test Requirements**:

  * `programmatic` TR-4.1: Reel-in movement is smooth and continuous

  * `human-judgement` TR-4.2: Reel-in feels responsive and visually appealing

## [x] Task 5: Implement Throw-back and Damage Mechanics

* **Priority**: P1

* **Depends On**: Task 4

* **Description**:

  * Calculate throw-back velocity based on player's view direction

  * Apply throw-back force to entity

  * Deal 6 armor piercing damage to entity

* **Success Criteria**:

  * Entity is thrown back in direction opposite to player's view

  * 6 armor piercing damage is applied

  * Damage bypasses armor correctly

* **Test Requirements**:

  * `programmatic` TR-5.1: Entity takes exactly 6 armor piercing damage

  * `human-judgement` TR-5.2: Throw-back distance and direction feel appropriate

## [x] Task 6: Client-Server Synchronization

* **Priority**: P1

* **Depends On**: Task 4, Task 5

* **Description**:

  * Implement packet system for ability activation

  * Synchronize entity movement and damage between client and server

* **Success Criteria**:

  * Abilities work correctly in multiplayer

  * No desynchronization issues

  * Packets are sent efficiently

* **Test Requirements**:

  * `programmatic` TR-6.1: Abilities work identically in singleplayer and multiplayer

  * `human-judgement` TR-6.2: No visible lag or desync during ability use

## [x] Task 7: Add Cooldowns and Balance

* **Priority**: P2

* **Depends On**: All previous tasks

* **Description**:

  * Implement cooldowns for both abilities

  * Balance ability usage to prevent spamming

  * Add visual/audio feedback for cooldowns

* **Success Criteria**:

  * Abilities have reasonable cooldowns

  * Players cannot spam abilities

  * Cooldowns are clearly indicated

* **Test Requirements**:

  * `programmatic` TR-7.1: Cooldowns prevent ability use until expired

  * `human-judgement` TR-7.2: Cooldown duration feels balanced

## [/] Task 8: Testing and Bug Fixing

* **Priority**: P2

* **Depends On**: All previous tasks

* **Description**:

  * Test all abilities thoroughly

  * Fix any bugs or edge cases

  * Optimize performance

* **Success Criteria**:

  * No crashes or major bugs

  * Abilities work consistently

  * No performance issues

* **Test Requirements**:

  * `programmatic` TR-8.1: Mod runs without crashes

  * `human-judgement` TR-8.2: Abilities feel polished and fun to use

## Implementation Notes

* Use Fabric API's keybinding system for keybind registration

* For entity detection, use raycasting with angle calculations

* For flutter jump, modify player velocity while spacebar is held in midair

* For reel-in, use smooth interpolation for entity movement

* For armor piercing damage, use appropriate damage source that bypasses armor

* Ensure all client-side changes are properly synchronized with server

* Test with various entity types to ensure compatibility

