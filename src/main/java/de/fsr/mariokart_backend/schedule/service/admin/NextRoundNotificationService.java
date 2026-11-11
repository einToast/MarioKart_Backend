package de.fsr.mariokart_backend.schedule.service.admin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.LocalizedNotification;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.NotificationTextService;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class NextRoundNotificationService {

    private final RoundRepository roundRepository;
    private final PointsRepository pointsRepository;
    private final TeamRepository teamRepository;
    private final PublicSettingsReadService publicSettingsReadService;
    private final AdminNotificationCreateService adminNotificationCreateService;
    private final NotificationTextService notificationTextService;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void onNextRoundChanged(NextRoundChangedEvent event) {
        sendNotificationForNextRound();
    }

    public void sendNotificationForNextRound() {
        List<Round> unplayedRounds = roundRepository.findByPlayedFalse();
        if (unplayedRounds.isEmpty()) {
            return;
        }
        unplayedRounds.sort(Comparator.comparing(Round::getRoundNumber));
        sendNotificationForNextRound(unplayedRounds.getFirst());
    }

    public void sendNotificationForNextRound(Round round) {
        if (round.getBreakTime() != null && !round.getBreakTime().isBreakEnded()) {
            notifyAll(language -> new NotificationRequestDTO(
                    notificationTextService.text("notification.break.title", language),
                    notificationTextService.text("notification.break.body", language)));
            return;
        }

        TournamentDTO settings = publicSettingsReadService.getSettings();
        Map<Long, LocalizedNotification> notificationsByTeam = new HashMap<>();
        for (Points point : pointsRepository.findByGameRoundId(round.getId())) {
            int switchIndex = point.getGame().getSwitchIndex();
            notificationsByTeam.put(point.getTeam().getId(), language -> {
                String title = notificationTextService.text("notification.round.play.title", language,
                        settings.switchName(switchIndex, language));
                return new NotificationRequestDTO(title,
                        notificationTextService.text("notification.round.play.body", language, title));
            });
        }
        LocalizedNotification pause = language -> new NotificationRequestDTO(
                notificationTextService.text("notification.round.pause.title", language),
                notificationTextService.text("notification.round.pause.body", language));
        for (Team team : teamRepository.findAll()) {
            notificationsByTeam.putIfAbsent(team.getId(), pause);
        }

        try {
            adminNotificationCreateService.sendNotificationsToTeams(notificationsByTeam);
        } catch (NotificationNotSentException e) {
            log.warn("Could not notify the teams about the next round: {}", e.getMessage());
        }
    }

    private void notifyAll(LocalizedNotification notification) {
        try {
            adminNotificationCreateService.sendNotificationToAll(notification);
        } catch (NotificationNotSentException e) {
            log.warn("Could not notify all teams: {}", e.getMessage());
        }
    }
}
