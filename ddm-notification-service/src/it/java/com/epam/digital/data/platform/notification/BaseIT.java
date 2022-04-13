package com.epam.digital.data.platform.notification;

import com.epam.digital.data.platform.notification.config.KafkaContextInitializer;
import com.epam.digital.data.platform.notification.config.WireMockContextInitializer;
import com.epam.digital.data.platform.notification.repository.NotificationTemplateRepository;
import com.epam.digital.data.platform.notification.repository.entity.NotificationTemplate;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(properties = {"data-platform.kafka.enabled=false"})
@ActiveProfiles("test")
@ContextConfiguration(initializers = {
    WireMockContextInitializer.class,
    KafkaContextInitializer.class})
public abstract class BaseIT {

  @RegisterExtension
  public static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      .withConfiguration(GreenMailConfiguration.aConfig().withUser("username", "password"))
      .withPerMethodLifecycle(false);

  @Autowired
  protected NotificationTemplateRepository repository;

  @SneakyThrows
  public static String jsonToStr(String content) {
    return Files.readString(
        Paths.get(BaseIT.class.getResource(content).toURI()), StandardCharsets.UTF_8);
  }

  public NotificationTemplate createEmailTemplateInDb(String name, String content) {
    var template = NotificationTemplate.builder()
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .channel("email")
        .checksum("1234")
        .content(content)
        .name(name)
        .id(UUID.randomUUID())
        .build();
    return repository.save(template);
  }
}
