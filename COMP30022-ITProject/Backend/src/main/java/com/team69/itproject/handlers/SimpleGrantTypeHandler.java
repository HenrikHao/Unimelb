package com.team69.itproject.handlers;

import com.alibaba.fastjson.TypeReference;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class SimpleGrantTypeHandler extends ListTypeHandler<SimpleGrantedAuthority> {

    @Override
    protected TypeReference<List<SimpleGrantedAuthority>> specificType() {
        return new TypeReference<List<SimpleGrantedAuthority>>() {
        };
    }


}
