package me.difficulty.core.mobs;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomMobRegistry {

    private final Map<String, CustomMob> mobs =
            new HashMap<>();

    private final NamespacedKey mobIdKey;

    public CustomMobRegistry(
            JavaPlugin plugin
    ) {

        this.mobIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_mob_id"
                );
    }

    public void register(
            CustomMob mob
    ) {

        mobs.put(
                mob.getId(),
                mob
        );
    }

    public Optional<CustomMob> get(
            String id
    ) {

        return Optional.ofNullable(
                mobs.get(id)
        );
    }

    public Optional<String> getMobId(
            LivingEntity entity
    ) {

        if (entity == null) {
            return Optional.empty();
        }

        String id =
                entity.getPersistentDataContainer().get(
                        mobIdKey,
                        PersistentDataType.STRING
                );

        return Optional.ofNullable(id);
    }

    public boolean is(
            LivingEntity entity,
            String id
    ) {

        return getMobId(entity)
                .map(id::equals)
                .orElse(false);
    }
}