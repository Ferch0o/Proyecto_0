package me.difficulty.features.pets;

import me.difficulty.day.DayManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetBehaviorTask extends BukkitRunnable {

    private final DayManager dayManager;

    /*
     * Cooldown para evitar que gatos y lobos
     * ataquen demasiadas veces.
     */
    private final Map<UUID, Long> attackCooldowns =
            new HashMap<>();


    public PetBehaviorTask(
            DayManager dayManager
    ) {

        this.dayManager = dayManager;
    }


    @Override
    public void run() {

        /*
         * ========================================
         * SOLO DESDE EL DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * ========================================
         * RECORRER TODOS LOS MUNDOS
         * ========================================
         */

        for (World world : Bukkit.getWorlds()) {


            /*
             * ====================================
             * LOBOS / PERROS
             * ====================================
             */

            for (Wolf wolf :
                    world.getEntitiesByClass(
                            Wolf.class
                    )) {

                updateWolf(
                        wolf
                );
            }


            /*
             * ====================================
             * GATOS
             * ====================================
             */

            for (Cat cat :
                    world.getEntitiesByClass(
                            Cat.class
                    )) {

                updateCat(
                        cat
                );
            }
        }


        /*
         * ========================================
         * LIMPIAR COOLDOWNS VIEJOS
         * ========================================
         */

        long now =
                System.currentTimeMillis();


        attackCooldowns
                .entrySet()
                .removeIf(
                        entry ->
                                entry.getValue()
                                        < now - 5000
                );
    }


    /*
     * ========================================
     * LOBO / PERRO
     * ========================================
     */

    private void updateWolf(
            Wolf wolf
    ) {

        /*
         * ========================================
         * LOBO DOMESTICADO
         * ========================================
         *
         * IMPORTANTE:
         *
         * Cuando ya está domesticado NO tocamos:
         *
         * - setAngry()
         * - setTarget()
         * - Pathfinder
         *
         * Dejamos toda su IA vanilla funcionando.
         *
         * De esta manera:
         *
         * - sigue al dueño
         * - se sienta
         * - se teletransporta
         * - defiende al dueño
         * - ataca al enemigo que golpea el dueño
         * - ataca a quien golpea al dueño
         */

        if (wolf.isTamed()) {


            /*
             * ====================================
             * FUERZA IV
             * ====================================
             */

            addStrength(
                    wolf
            );


            /*
             * ====================================
             * ARMADURA DE ARMADILLO
             * ====================================
             *
             * Si lleva Wolf Armor:
             *
             * → Resistencia III
             */

            ItemStack bodyArmor =
                    wolf.getEquipment()
                            .getItem(
                                    EquipmentSlot.BODY
                            );


            if (bodyArmor != null
                    && bodyArmor.getType()
                    == Material.WOLF_ARMOR) {

                wolf.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.RESISTANCE,
                                40,
                                1,
                                false,
                                true
                        )
                );
            }


            /*
             * Aquí terminamos.
             *
             * No modificamos absolutamente
             * nada más del lobo domesticado.
             */
            return;
        }


        /*
         * ========================================
         * LOBO SALVAJE
         * ========================================
         *
         * Aquí SÍ utilizamos nuestro sistema
         * personalizado.
         *
         * El lobo:
         *
         * - permanece domesticable
         * - no usa la ira vanilla
         * - persigue jugadores manualmente
         */

        wolf.setAngry(
                false
        );


        /*
         * Nunca dejamos que tenga un jugador
         * como target vanilla mientras
         * siga siendo salvaje.
         */

        if (wolf.getTarget()
                instanceof Player) {

            wolf.setTarget(
                    null
            );
        }


        /*
         * ========================================
         * BUSCAR JUGADOR
         * ========================================
         */

        Player target =
                findNearestPlayer(
                        wolf,
                        16
                );


        /*
         * ========================================
         * NO HAY JUGADOR CERCA
         * ========================================
         */

        if (target == null) {

            wolf.getPathfinder()
                    .stopPathfinding();

            return;
        }


        /*
         * ========================================
         * PERSEGUIR JUGADOR
         * ========================================
         *
         * Únicamente utilizamos Pathfinder.
         *
         * NO usamos:
         *
         * wolf.setTarget(player)
         *
         * porque volvería a activar
         * el sistema de ira vanilla.
         */

        wolf.getPathfinder()
                .moveTo(
                        target,
                        1.2
                );


        /*
         * ========================================
         * ATAQUE PERSONALIZADO
         * ========================================
         */

        tryWildWolfAttack(
                wolf,
                target
        );
    }


    /*
     * ========================================
     * ATAQUE DEL LOBO SALVAJE
     * ========================================
     */

    private void tryWildWolfAttack(
            Wolf wolf,
            Player target
    ) {

        /*
         * ========================================
         * DISTANCIA
         * ========================================
         */

        if (wolf.getLocation()
                .distanceSquared(
                        target.getLocation()
                ) > 4.0) {

            return;
        }


        /*
         * No atacar atravesando paredes.
         */
        if (!wolf.hasLineOfSight(
                target
        )) {

            return;
        }


        /*
         * ========================================
         * COOLDOWN
         * ========================================
         */

        long now =
                System.currentTimeMillis();


        long nextAttack =
                attackCooldowns.getOrDefault(
                        wolf.getUniqueId(),
                        0L
                );


        if (now < nextAttack) {
            return;
        }


        /*
         * ========================================
         * DAÑO
         * ========================================
         */

        double damage =
                4.0;


        AttributeInstance attackDamage =
                wolf.getAttribute(
                        Attribute.ATTACK_DAMAGE
                );


        if (attackDamage != null) {

            damage =
                    attackDamage.getValue();
        }


        /*
         * ========================================
         * MUY IMPORTANTE
         * ========================================
         *
         * Antes hacíamos:
         *
         * target.damage(damage, wolf);
         *
         * Ahora NO ponemos al lobo como
         * fuente del daño.
         *
         * Así evitamos activar cualquier
         * comportamiento vanilla relacionado
         * con combate / enfado.
         */

        target.damage(
                damage
        );


        /*
         * ========================================
         * DEBILIDAD IV
         * ========================================
         *
         * La aplicamos directamente aquí,
         * porque el daño ya no tiene al lobo
         * como damager.
         */

        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        10 * 20,
                        3,
                        false,
                        true
                )
        );


        /*
         * El lobo debe continuar completamente
         * neutral para Minecraft.
         */

        wolf.setAngry(
                false
        );

        wolf.setTarget(
                null
        );


        /*
         * ========================================
         * 1 SEGUNDO ENTRE ATAQUES
         * ========================================
         */

        attackCooldowns.put(
                wolf.getUniqueId(),
                now + 1000L
        );
    }


    /*
     * ========================================
     * GATO
     * ========================================
     */

    private void updateCat(
            Cat cat
    ) {

        /*
         * ====================================
         * GATO DOMESTICADO
         * ====================================
         */

        if (cat.isTamed()) {


            /*
             * Si antes de domesticarlo
             * perseguía a un jugador,
             * eliminamos ese objetivo.
             */
            if (cat.getTarget()
                    instanceof Player) {

                cat.setTarget(
                        null
                );

                cat.getPathfinder()
                        .stopPathfinding();
            }


            /*
             * ====================================
             * FUERZA IV
             * ====================================
             */

            addStrength(
                    cat
            );


            /*
             * ====================================
             * BUSCAR CREEPER
             * ====================================
             */

            Creeper creeper =
                    findNearestCreeper(
                            cat,
                            16
                    );


            /*
             * ====================================
             * HAY CREEPER CERCA
             * ====================================
             */

            if (creeper != null) {

                cat.setSitting(
                        false
                );


                cat.setTarget(
                        creeper
                );


                cat.getPathfinder()
                        .moveTo(
                                creeper,
                                1.2
                        );


                tryAttack(
                        cat,
                        creeper
                );


                return;
            }


            /*
             * ====================================
             * YA NO HAY CREEPER
             * ====================================
             */

            if (cat.getTarget()
                    instanceof Creeper) {

                cat.setTarget(
                        null
                );

                cat.getPathfinder()
                        .stopPathfinding();
            }


            return;
        }


        /*
         * ====================================
         * GATO SALVAJE
         * ====================================
         */

        Player target =
                findNearestPlayer(
                        cat,
                        16
                );


        /*
         * ====================================
         * NO HAY JUGADOR CERCA
         * ====================================
         */

        if (target == null) {

            if (cat.getTarget()
                    instanceof Player) {

                cat.setTarget(
                        null
                );

                cat.getPathfinder()
                        .stopPathfinding();
            }

            return;
        }


        /*
         * Evitamos que esté sentado.
         */
        cat.setSitting(
                false
        );


        /*
         * ====================================
         * PERSEGUIR AL JUGADOR
         * ====================================
         */

        cat.setTarget(
                target
        );


        cat.getPathfinder()
                .moveTo(
                        target,
                        1.25
                );


        /*
         * Atacar cuando esté cerca.
         */
        tryAttack(
                cat,
                target
        );
    }


    /*
     * ========================================
     * ATAQUE NORMAL DEL GATO
     * ========================================
     */

    private void tryAttack(
            LivingEntity attacker,
            LivingEntity target
    ) {

        /*
         * Máximo 2 bloques.
         */
        if (attacker.getLocation()
                .distanceSquared(
                        target.getLocation()
                ) > 4.0) {

            return;
        }


        /*
         * No atacar a través de paredes.
         */
        if (!attacker.hasLineOfSight(
                target
        )) {

            return;
        }


        /*
         * ====================================
         * COOLDOWN
         * ====================================
         */

        long now =
                System.currentTimeMillis();


        long nextAttack =
                attackCooldowns.getOrDefault(
                        attacker.getUniqueId(),
                        0L
                );


        if (now < nextAttack) {
            return;
        }


        /*
         * ====================================
         * ATAQUE REAL
         * ====================================
         */

        attacker.attack(
                target
        );


        /*
         * 1 segundo entre ataques.
         */
        attackCooldowns.put(
                attacker.getUniqueId(),
                now + 1000L
        );
    }


    /*
     * ========================================
     * FUERZA II
     * ========================================
     */

    private void addStrength(LivingEntity entity) {
        entity.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.STRENGTH,
                        40,
                        1,
                        false,
                        true
                )
        );
    }


    /*
     * ========================================
     * BUSCAR JUGADOR MÁS CERCANO
     * ========================================
     */

    private Player findNearestPlayer(
            LivingEntity entity,
            double range
    ) {

        Player nearest =
                null;


        double nearestDistance =
                range * range;


        for (Player player :
                entity.getWorld()
                        .getPlayers()) {


            /*
             * Ignoramos:
             *
             * - jugadores muertos
             * - Creative
             * - Spectator
             */
            if (player.isDead()
                    || player.getGameMode()
                    == GameMode.CREATIVE
                    || player.getGameMode()
                    == GameMode.SPECTATOR) {

                continue;
            }


            double distance =
                    entity.getLocation()
                            .distanceSquared(
                                    player.getLocation()
                            );


            if (distance
                    < nearestDistance) {

                nearestDistance =
                        distance;

                nearest =
                        player;
            }
        }


        return nearest;
    }


    /*
     * ========================================
     * BUSCAR CREEPER MÁS CERCANO
     * ========================================
     */

    private Creeper findNearestCreeper(
            Cat cat,
            double range
    ) {

        Creeper nearest =
                null;


        double nearestDistance =
                range * range;


        for (Creeper creeper :
                cat.getLocation()
                        .getNearbyEntitiesByType(
                                Creeper.class,
                                range
                        )) {


            if (creeper.isDead()) {
                continue;
            }


            double distance =
                    cat.getLocation()
                            .distanceSquared(
                                    creeper.getLocation()
                            );


            if (distance
                    < nearestDistance) {

                nearestDistance =
                        distance;

                nearest =
                        creeper;
            }
        }


        return nearest;
    }
}