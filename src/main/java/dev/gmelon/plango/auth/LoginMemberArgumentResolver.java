package dev.gmelon.plango.auth;


import dev.gmelon.plango.auth.dto.SessionMember;
import dev.gmelon.plango.exception.UnauthenticatedException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginMemberAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
        boolean isSessionMemberType = SessionMember.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginMemberAnnotation && isSessionMemberType;
    }

    @Override
    public SessionMember resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionMember.SESSION_NAME) == null) {
            throw new UnauthenticatedException();
        }

        return (SessionMember) session.getAttribute(SessionMember.SESSION_NAME);
    }
}
