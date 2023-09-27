package com.dws.challenge;


import com.dws.challenge.service.EmailNotificationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class AccountsServiceTestConfiguration {
    @Bean
    @Primary
    public EmailNotificationService emailNotificationService() {
        return Mockito.mock(EmailNotificationService.class);
    }
}
