package com.epam.digital.data.platform.notification.service;

import static org.mockito.Mockito.*;

import java.util.UUID;

import com.epam.digital.data.platform.notification.core.repository.CoreNotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

  private NotificationTemplateService notificationTemplateService;

  @Mock
  private CoreNotificationTemplateRepository notificationTemplateRepository;

  @BeforeEach
  void setup() {
    notificationTemplateService = new NotificationTemplateService(notificationTemplateRepository);
  }

  @Test
  void getAllTest() {
    notificationTemplateService.getAll();

    verify(notificationTemplateRepository, times(1)).findAllProjectedBy();
  }

  @Test
  void deleteTest() {
    UUID uuid = UUID.randomUUID();
    notificationTemplateService.delete(uuid);

    verify(notificationTemplateRepository, times(1)).deleteById(uuid);
  }
}
