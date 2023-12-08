package dev.gmelon.plango.config.security;

import dev.gmelon.plango.domain.member.MemberRole;
import dev.gmelon.plango.domain.member.MemberType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@WithSecurityContext(factory = PlangoMockSecurityContext.class)
public @interface PlangoMockUser {

    String email() default "a@a.com";

    String password() default "passwordA";

    String nickname() default "nameA";

    MemberRole role() default MemberRole.ROLE_USER;

    MemberType type() default MemberType.EMAIL;

    boolean termsAccepted() default true;

}
