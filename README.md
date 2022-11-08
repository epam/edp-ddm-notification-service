# ddm-notification-service

### Overview

The main purpose of the service is to send notifications to recipients. The application listens
kafka topic and sends notification to a recipient using allowed available channels such as email
etc.

The workflow:

* Get Kafka record
* Get recipient user settings to figure out which channels are available for notification
* Get notification template and fill it by received model
* Send notification using filled template to an appropriate channel

### Usage

#### Prerequisites:

* Kafka is configured and running.
* user-settings-service is configured and running.
* Postgres database is configured and running;
* Keycloak is configured and running;

#### Configuration

Available properties are following:

```yaml
notifications:
  enabled: (boolean) - whether notifications should be enabled or not

data-platform:
  kafka:
  #properties according to ddm-starter-kafka
  datasource:
  #properties according to ddm-starter-database

spring:
  mail: # spring mail properties for email notification
    host: localhost
    port: 3025
    username: username
    password: password
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
          starttls:
            enable: true

user-settings-service:
  url: base user settings url

keycloak:
  url: base keycloak url
  system-user:
    realm: system user realm name
    client-id: system user client identifier
    client-secret: system user client secret
```

#### Run application:

* `java -jar <file-name>.jar`

### Local development

Run spring boot application using 'local' profile:

* `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
* `application-local.yml` - configuration file for local profile.

### Test execution

* Tests could be run via maven command:
    * `mvn verify -P test` OR using appropriate functions of your IDE.

### License

The ddm-notification-service is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
