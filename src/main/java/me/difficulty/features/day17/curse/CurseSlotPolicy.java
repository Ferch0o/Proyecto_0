package me.difficulty.features.day17.curse;

import java.util.Set;

public final class CurseSlotPolicy {

    private static final Set<Integer> DAY_17_SLOTS =
            Set.of(1, 7);

    private static final Set<Integer> DAY_25_SLOTS =
            Set.of(1, 7, 9, 17, 18, 26);

    private CurseSlotPolicy() {
    }

    public static Set<Integer> getBlockedSlots(
            int currentDay,
            CurseState state
    ) {

        // Antes del día 17 no hay bloqueos.
        if (currentDay < 17) {
            return Set.of();
        }

        // Los jugadores curados recuperan sus slots.
        if (!state.isCursed()) {
            return Set.of();
        }

        // Maldición agravada desde el día 25.
        if (currentDay >= 25) {
            return DAY_25_SLOTS;
        }

        // Maldición inicial desde el día 17.
        return DAY_17_SLOTS;
    }
}