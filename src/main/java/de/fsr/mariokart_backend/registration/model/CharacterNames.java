package de.fsr.mariokart_backend.registration.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CharacterNames {

    public static final List<String> ALL = List.of(
            "Baby-Daisy",
            "Baby-Luigi",
            "Baby-Mario",
            "Baby-Peach",
            "Baby-Rosalina",
            "Bewohner",
            "Bewohnerin",
            "Bowser",
            "Bowser-Jr",
            "Daisy",
            "Donkey-Kong",
            "Iggy-Koopa",
            "Inkling-Junge",
            "Inkling-Mädchen",
            "Katzen-Peach",
            "Knochen-Bowser",
            "Knochentrocken",
            "Koopa",
            "König-Buu-Huu",
            "Lakitu",
            "Larry-Koopa",
            "Lemmy-Koopa",
            "Link",
            "Ludwig-von-Koopa",
            "Luigi",
            "Mario",
            "Melinda",
            "Metall-Mario",
            "Morton-Koopa-Jr",
            "Peach",
            "Rosagold-Peach",
            "Rosalina",
            "Roy-Koopa",
            "Shy-Guy",
            "Tanuki-Mario",
            "Toad",
            "Toadette",
            "Waluigi",
            "Wario",
            "Wendy-O.-Koopa",
            "Yoshi");

    public static final Set<String> HYPHENATED_INGAME_NAMES = Set.of(
            "Inkling-Junge",
            "Inkling-Mädchen",
            "Katzen-Peach",
            "Knochen-Bowser",
            "Metall-Mario",
            "Rosagold-Peach",
            "Tanuki-Mario");

    public static final Map<String, String> DIFFERENT_INGAME_NAMES = Map.of(
            "Bowser-Jr", "Bowser Jr.",
            "Iggy-Koopa", "Iggy",
            "Larry-Koopa", "Larry",
            "Lemmy-Koopa", "Lemmy",
            "Ludwig-von-Koopa", "Ludwig",
            "Morton-Koopa-Jr", "Morton",
            "Roy-Koopa", "Roy",
            "Wendy-O.-Koopa", "Wendy");

    private CharacterNames() {
    }
}
