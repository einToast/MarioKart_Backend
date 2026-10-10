package de.fsr.mariokart_backend.settings.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.SwitchDTO;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSettingsUpdateServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @InjectMocks
    private AdminSettingsUpdateService service;

    @Test
    void updateSettingsUpdatesProvidedFields() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, false, false, 4, SurveyKeyMode.DISABLED);
        Tournament saved = new Tournament(1L, true, true, 8, SurveyKeyMode.DISABLED);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(tournamentRepository.save(existing)).thenReturn(saved);

        TournamentDTO result = service.updateSettings(new TournamentDTO(true, true, 8, null));

        assertThat(result.getTournamentOpen()).isTrue();
        assertThat(result.getRegistrationOpen()).isTrue();
        assertThat(result.getMaxGamesCount()).isEqualTo(8);
    }

    @Test
    void updateSettingsUpdatesSurveyKeySettings() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(tournamentRepository.save(existing)).thenReturn(existing);

        TournamentDTO result = service.updateSettings(
                new TournamentDTO(null, null, null, SurveyKeyMode.REQUIRED));

        assertThat(result.getSurveyKeyMode()).isEqualTo(SurveyKeyMode.REQUIRED);
        assertThat(result.getTournamentOpen()).isTrue();
        assertThat(result.getMaxGamesCount()).isEqualTo(4);
    }

    @Test
    void updateSettingsKeepsSurveyKeySettingsWhenNotProvided() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, false, false, 4, SurveyKeyMode.DISTRIBUTING);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(tournamentRepository.save(existing)).thenReturn(existing);

        TournamentDTO result = service.updateSettings(new TournamentDTO(true, null, null, null));

        assertThat(result.getSurveyKeyMode()).isEqualTo(SurveyKeyMode.DISTRIBUTING);
    }

    @Test
    void newTournamentDefaultsToDisabledSurveyKeyMode() {
        TournamentDTO dto = new TournamentDTO(new Tournament());

        assertThat(dto.getSurveyKeyMode()).isEqualTo(SurveyKeyMode.DISABLED);
    }

    @Test
    void updateSettingsThrowsConflictWhenReopeningRegistrationAfterScheduleCreation() {
        Tournament existing = new Tournament(1L, false, false, 4, SurveyKeyMode.DISABLED);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.updateSettings(new TournamentDTO(null, true, null, null)))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Matches already exist");
    }

    @Test
    void newTournamentDefaultsToFourFinalTeamsAndNoLayout() {
        TournamentDTO dto = new TournamentDTO(new Tournament());

        assertThat(dto.getFinalTeamsCount()).isEqualTo(4);
        assertThat(dto.getSwitches()).isEmpty();
        assertThat(dto.getFloorPlan()).isNull();
        assertThat(dto.getProgram()).isNull();
    }

    @Test
    void updateSettingsStoresSwitchesFloorPlanAndFinalTeams() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        TournamentDTO update = new TournamentDTO();
        update.setFinalTeamsCount(8);
        update.setSwitches(List.of(new SwitchDTO(" Blau ", "#9DAEDA"), new SwitchDTO("Rot", "#da9dc9")));
        update.setFloorPlan("{\"elements\":[]}");

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(false);
        when(tournamentRepository.save(existing)).thenReturn(existing);

        TournamentDTO result = service.updateSettings(update);

        assertThat(result.getFinalTeamsCount()).isEqualTo(8);
        assertThat(result.getSwitches())
                .containsExactly(new SwitchDTO("Blau", "#9DAEDA"), new SwitchDTO("Rot", "#da9dc9"));
        assertThat(result.getFloorPlan()).isEqualTo("{\"elements\":[]}");
        assertThat(result.getTournamentOpen()).isTrue();
    }

    @Test
    void updateSettingsKeepsLayoutWhenNotProvided() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        existing.setFloorPlan("{}");
        existing.getSwitches().add(new de.fsr.mariokart_backend.settings.model.SwitchConfig("Blau", "#9DAEDA"));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(tournamentRepository.save(existing)).thenReturn(existing);

        TournamentDTO result = service.updateSettings(new TournamentDTO(false, null, null, null));

        assertThat(result.getSwitches()).containsExactly(new SwitchDTO("Blau", "#9DAEDA"));
        assertThat(result.getFloorPlan()).isEqualTo("{}");
    }

    @Test
    void updateSettingsRemovesTheFloorPlanWhenBlank() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        existing.setFloorPlan("{}");
        TournamentDTO update = new TournamentDTO();
        update.setFloorPlan("");

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(tournamentRepository.save(existing)).thenReturn(existing);

        assertThat(service.updateSettings(update).getFloorPlan()).isNull();
    }

    @Test
    void updateSettingsStoresAndRemovesTheProgram() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        TournamentDTO update = new TournamentDTO();
        update.setProgram("{\"entries\":[]}");
        TournamentDTO removal = new TournamentDTO();
        removal.setProgram(" ");

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(tournamentRepository.save(existing)).thenReturn(existing);

        assertThat(service.updateSettings(update).getProgram()).isEqualTo("{\"entries\":[]}");
        assertThat(service.updateSettings(new TournamentDTO(true, null, null, null)).getProgram())
                .isEqualTo("{\"entries\":[]}");
        assertThat(service.updateSettings(removal).getProgram()).isNull();
    }

    @Test
    void updateSettingsRejectsAnOversizedProgram() {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        TournamentDTO update = new TournamentDTO();
        update.setProgram("x".repeat(AdminSettingsUpdateService.MAX_PROGRAM_LENGTH + 1));

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));

        assertThatThrownBy(() -> service.updateSettings(update)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateSettingsRejectsInvalidSwitches() {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));

        TournamentDTO blankName = new TournamentDTO();
        blankName.setSwitches(List.of(new SwitchDTO("  ", "#9DAEDA")));
        TournamentDTO invalidColor = new TournamentDTO();
        invalidColor.setSwitches(List.of(new SwitchDTO("Blau", "blue")));
        TournamentDTO tooMany = new TournamentDTO();
        tooMany.setSwitches(java.util.Collections.nCopies(17, new SwitchDTO("Blau", "#9DAEDA")));

        assertThatThrownBy(() -> service.updateSettings(blankName)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.updateSettings(invalidColor)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.updateSettings(tooMany)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateSettingsRejectsFinalTeamsOutOfRange() {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));

        TournamentDTO tooFew = new TournamentDTO();
        tooFew.setFinalTeamsCount(1);
        TournamentDTO tooMany = new TournamentDTO();
        tooMany.setFinalTeamsCount(9);

        assertThatThrownBy(() -> service.updateSettings(tooFew)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.updateSettings(tooMany)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateSettingsThrowsConflictWhenChangingFinalTeamsAfterFinalCreation() {
        Tournament existing = new Tournament(1L, true, false, 4, SurveyKeyMode.DISABLED);
        TournamentDTO update = new TournamentDTO();
        update.setFinalTeamsCount(6);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isFinalScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.updateSettings(update))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Final schedule already created");
    }

    @Test
    void updateSettingsThrowsWhenSettingsMissing() {
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> service.updateSettings(new TournamentDTO(true, true, 3, null)))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
