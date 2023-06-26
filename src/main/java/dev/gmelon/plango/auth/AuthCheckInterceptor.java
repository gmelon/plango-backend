package dev.gmelon.plango.auth;

import dev.gmelon.plango.auth.dto.SessionMember;
import dev.gmelon.plango.exception.UnauthenticatedException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionMember.SESSION_NAME) == null) {
            throw new UnauthenticatedException();
        }

        return true;
    }
}
