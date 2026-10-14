package me.difficulty.mobs;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CustomMob {

    private final String id;
    private final NamespacedKey mobIdKey;

    protected CustomMob(
            JavaPlugin plugin,
            String id
    ) {

        this.id = id;

        this.mobIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_mob_id"
                );
    }

    /*
     * Aplica la configuración completa
     * del Custom Mob.
     */
    public final void apply(
            LivingEntity entity
    ) {

        /*
         * Primero guardamos la identidad
         * del mob.
         */
        entity.getPersistentDataContainer().set(
                mobIdKey,
                PersistentDataType.STRING,
                id
        );

        /*
         * Después cada mob configura
         * sus propias características.
         */
        configure(entity);
    }

    /*
     * Comprueba si una entidad pertenece
     * a este Custom Mob.
     */
    public boolean isMob(
            LivingEntity entity
    ) {

        String storedId =
                entity.getPersistentDataContainer().get(
                        mobIdKey,
                        PersistentDataType.STRING
                );

        return id.equals(storedId);
    }

    public String getId() {
        return id;
    }

    /*
     * Cada Custom Mob implementará
     * su propia configuración.
     */
    protected abstract void configure(
            LivingEntity entity
    );
}