package de.fsr.mariokart_backend.schedule.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("unit")
class FieldPointsMultiplierTest {

    @ParameterizedTest
    @CsvSource({
            "1, 15.0",
            "2, 13.5",
            "4, 11.5",
            "8, 9.0",
            "12, 6.8333"
    })
    void averagePointsAssumeTheTeamsTakeTheBestPlaces(int teamCount, double average) {
        assertThat(FieldPointsMultiplier.averagePoints(teamCount)).isCloseTo(average, within(0.0001));
    }

    @Test
    void averagePointsStayInsideTheTwelvePlacesOfARace() {
        assertThat(FieldPointsMultiplier.MAX_TEAMS).isEqualTo(12);
        assertThat(FieldPointsMultiplier.averagePoints(13)).isEqualTo(FieldPointsMultiplier.averagePoints(12));
        assertThat(FieldPointsMultiplier.averagePoints(0)).isEqualTo(15.0);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 4, 1.0",
            "8, 4, 1.2778",
            "12, 4, 1.6829",
            "12, 8, 1.3171",
            "4, 2, 1.1739"
    })
    void fieldsWithMoreTeamsThanTheSmallestCountMore(int teamCount, int baseTeamCount, double multiplier) {
        assertThat(FieldPointsMultiplier.of(teamCount, baseTeamCount)).isCloseTo(multiplier, within(0.0001));
    }

    @Test
    void pointsCountAsTheyAreWithoutAKnownNumberOfTeams() {
        assertThat(FieldPointsMultiplier.of(0, 4)).isEqualTo(1.0);
        assertThat(FieldPointsMultiplier.of(8, 0)).isEqualTo(1.0);
    }
}
