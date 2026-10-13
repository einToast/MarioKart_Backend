package de.fsr.mariokart_backend.schedule.model;

import java.util.Arrays;

/**
 * Makes points from fields with different numbers of teams comparable.
 *
 * The more teams share a race, the fewer points each of them can get on
 * average. The multiplier of a field is the average of the smallest field
 * divided by its own average, assuming the teams take the best places.
 */
public final class FieldPointsMultiplier {

    // Points for places 1 to 12 of a Mario Kart 8 race
    private static final int[] PLACE_POINTS = { 15, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };

    public static final int MAX_TEAMS = PLACE_POINTS.length;

    private FieldPointsMultiplier() {
    }

    public static double averagePoints(int teamCount) {
        int places = Math.clamp(teamCount, 1, MAX_TEAMS);
        return Arrays.stream(PLACE_POINTS, 0, places).average().orElseThrow();
    }

    /**
     * @param teamCount     teams on the field the points come from
     * @param baseTeamCount teams on the smallest field, whose points count as they
     *                      are
     */
    public static double of(int teamCount, int baseTeamCount) {
        if (teamCount <= 0 || baseTeamCount <= 0) {
            return 1;
        }
        return averagePoints(baseTeamCount) / averagePoints(teamCount);
    }
}
