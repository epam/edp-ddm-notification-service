package com.epam.digital.data.platform.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.notification.exception.NotificationTemplateNotFoundException;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailNotificationTemplateServiceTest {

  @Mock
  private NotificationTemplateRepository repository;

  private EmailNotificationTemplateService service;

  @BeforeEach
  void setUp() {
    service = new EmailNotificationTemplateService(repository);
  }

  @Test
  void shouldThrowNotFoundException() {
    var name = "invalid-name";

    when(repository.findByName(name)).thenReturn(null);

    var exception = assertThrows(NotificationTemplateNotFoundException.class,
        () -> service.getByName(name));

    assertThat(exception.getMessage())
        .isEqualTo(String.format(NotificationTemplateNotFoundException.MESSAGE_FORMAT, name));
  }
}
