package com.team69.itproject.aop;

import com.team69.itproject.aop.annotations.UserAuth;
import com.team69.itproject.aop.enums.AccessLevel;
import com.team69.itproject.utils.PublicUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Aspect
@Component
@Slf4j
public class UserAuthAspect {
    @Autowired
    private PublicUtils publicUtils;

    @Pointcut("@annotation(com.team69.itproject.aop.annotations.UserAuth)")
    public void userAuth() {
    }

    @Around("userAuth()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(authorization.lastIndexOf("Bearer") + 7);
        Claims claims = publicUtils.getClaimsFromToken(token);
        String id = claims.get("id").toString();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (!methodSignature.getMethod().isAnnotationPresent(PreAuthorize.class)) {
            return joinPoint.proceed();
        }
        UserAuth annotation = methodSignature.getMethod().getAnnotation(UserAuth.class);
        for (AccessLevel accessLevel : annotation.value()) {
            if (handleUserLevel(accessLevel, id, request) || handleAdminLevel(accessLevel, claims)) {
                return joinPoint.proceed();
            }
        }
        return null;
    }

    private boolean handleUserLevel(AccessLevel accessLevel, String id, HttpServletRequest request){
        if (accessLevel.equals(AccessLevel.SELF)){
            String userId = request.getHeader("User-Id");
            return userId.equals(id);
        }
        return false;
    }

    private boolean handleAdminLevel(AccessLevel accessLevel, Claims claims){
        if (accessLevel.equals(AccessLevel.ADMIN)){
            String role = claims.get("authorities").toString();
            return role.contains("admin");
        }
        return false;
    }
}
