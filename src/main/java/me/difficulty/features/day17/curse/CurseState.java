package me.difficulty.features.day17.curse;

public enum CurseState {

    // El jugador todavía tiene la maldición.
    CURSED,

    // El jugador consumió la Patata Elemental.
    CURED,

    // Maldición irreversible por inactividad.
    IRREVERSIBLE;

    public boolean isCursed() {
        return this != CURED;
    }

    public boolean canBeCured() {
        return this == CURSED;
    }
}