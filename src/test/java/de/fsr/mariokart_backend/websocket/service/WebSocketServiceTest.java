package de.fsr.mariokart_backend.websocket.service;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache scheduleCache;

    @InjectMocks
    private WebSocketService service;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void sendMessageDelegatesToMessagingTemplate() {
        service.sendMessage("/topic/messages", "hello");

        verify(messagingTemplate).convertAndSend("/topic/messages", "hello");
    }

    @Test
    void sendMessageClearsTheCachesBeforeTheClientsReload() {
        when(cacheManager.getCacheNames()).thenReturn(List.of("schedule"));
        when(cacheManager.getCache("schedule")).thenReturn(scheduleCache);

        service.sendMessage("/topic/rounds", "update");

        InOrder order = inOrder(scheduleCache, messagingTemplate);
        order.verify(scheduleCache).clear();
        order.verify(messagingTemplate).convertAndSend("/topic/rounds", "update");
    }

    @Test
    void sendMessageWaitsForTheTransactionToCommit() {
        TransactionSynchronizationManager.initSynchronization();

        service.sendMessage("/topic/rounds", "update");

        verify(messagingTemplate, never()).convertAndSend("/topic/rounds", "update");

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(messagingTemplate).convertAndSend("/topic/rounds", "update");
    }
}
