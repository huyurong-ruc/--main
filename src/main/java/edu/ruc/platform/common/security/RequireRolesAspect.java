package edu.ruc.platform.common.security;

import edu.ruc.platform.auth.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireRolesAspect {

    private final CurrentUserService currentUserService;

    @Before("@within(edu.ruc.platform.common.security.RequireRoles) || @annotation(edu.ruc.platform.common.security.RequireRoles)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequireRoles requireRoles = AnnotationUtils.findAnnotation(signature.getMethod(), RequireRoles.class);
        if (requireRoles == null) {
            Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
            if (targetClass != null) {
                requireRoles = AnnotationUtils.findAnnotation(targetClass, RequireRoles.class);
            }
        }
        if (requireRoles == null) {
            return;
        }
        currentUserService.requireAnyRole(requireRoles.value());
    }
}
