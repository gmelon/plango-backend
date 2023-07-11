package dev.gmelon.plango.config.auth.handler;

import dev.gmelon.plango.config.auth.exception.UnauthorizedException;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationFailureEventListener {

    @EventListener
    public void onFailure(AuthorizationDeniedEvent failure) {
        throw new UnauthorizedException();
    }

}
