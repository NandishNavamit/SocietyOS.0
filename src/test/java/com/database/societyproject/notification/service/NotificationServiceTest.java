package com.database.societyproject.notification.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.database.societyproject.notification.dto.NotificationRequest;
import com.database.societyproject.notification.entity.Notification;
import com.database.societyproject.notification.entity.NotificationType;
import com.database.societyproject.notification.repository.NotificationRepository;
import com.database.societyproject.occupancy.repository.FlatOccupancyRepository;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.society.entity.Society;
import com.database.societyproject.society.service.SocietyService;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final SocietyService societyService = mock(SocietyService.class);
    private final UserService userService = mock(UserService.class);
    private final NotificationService service = new NotificationService(repository, societyService, userService,
            mock(FlatOccupancyRepository.class), mock(EmailDeliveryService.class), mock(UserSocietyRoleRepository.class));

    @Test
    void manualNotificationIsStoredForTheRequestedUser() {
        Society society = mock(Society.class);
        User user = mock(User.class);
        when(societyService.findEntity(1L)).thenReturn(society);
        when(userService.findEntity(2L)).thenReturn(user);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new NotificationRequest(1L, 2L, NotificationType.ANNOUNCEMENT,
                "Water shutdown", "Water will be unavailable from 10 AM to noon."));

        verify(repository).save(argThat(notification -> notification.getSociety() == society
                && notification.getRecipientUser() == user
                && notification.getType() == NotificationType.ANNOUNCEMENT
                && !notification.getRead()));
    }

    @Test
    void markReadSetsTheReadFlagAndTimestamp() {
        Notification notification = new Notification();
        Society society = mock(Society.class);
        User user = mock(User.class);
        when(society.getId()).thenReturn(1L);
        when(user.getId()).thenReturn(2L);
        notification.setSociety(society);
        notification.setRecipientUser(user);
        notification.setType(NotificationType.ANNOUNCEMENT);
        notification.setTitle("Test");
        notification.setMessage("Test notification");
        notification.setRead(false);
        when(repository.findById(1L)).thenReturn(Optional.of(notification));

        service.markRead(1L);

        assertTrue(notification.getRead());
        assertTrue(notification.getReadAt() != null);
    }
}
