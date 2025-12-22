package pt.estga.security.aspects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.estga.shared.aop.SensitiveOperation;
import pt.estga.shared.exceptions.ReauthenticationRequiredException;

import java.time.Instant;

@Aspect
@Component
public class SensitiveOperationAspect {

    private static final long REAUTHENTICATION_TIMEOUT_SECONDS = 600; // 10 minutes

    @Before("@annotation(sensitiveOperation)")
    public void checkReauthentication(SensitiveOperation sensitiveOperation) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("reauthenticatedAt") == null) {
            throw new ReauthenticationRequiredException("Sensitive operation requires reauthentication");
        }

        long reauthenticatedAt = (long) session.getAttribute("reauthenticatedAt");
        long now = Instant.now().getEpochSecond();

        if ((now - reauthenticatedAt) > REAUTHENTICATION_TIMEOUT_SECONDS) {
            throw new ReauthenticationRequiredException("Reauthentication has expired");
        }
    }
}
