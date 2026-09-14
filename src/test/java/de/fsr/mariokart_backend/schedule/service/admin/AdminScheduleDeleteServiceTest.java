package de.fsr.mariokart_backend.schedule.service.admin;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.schedule.repository.RoundRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminScheduleDeleteServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private AdminScheduleDeleteService service;

    @Test
    void deleteScheduleDelegatesToRepository() {
        service.deleteSchedule();

        verify(roundRepository).deleteAll();
    }

    @Test
    void deleteFinalScheduleDelegatesToRepository() {
        service.deleteFinalSchedule();

        verify(roundRepository).deleteAllByFinalGameTrue();
    }
}
