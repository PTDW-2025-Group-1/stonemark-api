package pt.estga.user.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.estga.user.repositories.UserRepository;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class UserScheduler {

    private final UserService userService;

    @Value("${user.delete-after-days:60}")
    private int deleteAfterDays;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteUnverifiedUsers() {
        userService.deleteUnverifiedUsers(Instant.now().minus(deleteAfterDays, ChronoUnit.DAYS));
    }
}
