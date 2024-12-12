package de.fsr.mariokart_backend.settings.model.dto;

import de.fsr.mariokart_backend.settings.model.Tournament;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TournamentDTO {
    private Boolean tournamentOpen;
    private Boolean registrationOpen;
    private Integer maxGamesCount;

    public TournamentDTO(Tournament tournament) {
        this.tournamentOpen = tournament.isTournamentOpen();
        this.registrationOpen = tournament.isRegistrationOpen();
        this.maxGamesCount = tournament.getMaxGamesCount();
    }
}
