package com.team69.itproject.aop.annotations;

import com.team69.itproject.aop.enums.AccessLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAuth {

    AccessLevel[] value();
}
