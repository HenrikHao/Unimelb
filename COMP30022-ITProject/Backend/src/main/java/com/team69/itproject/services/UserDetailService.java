package com.team69.itproject.services;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.team69.itproject.entities.po.UserPO;
import com.team69.itproject.mappers.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<UserPO> queryWrapper = new LambdaQueryWrapper<>();
        UserPO userPO = usersMapper.selectOne(
                queryWrapper.select(
                                UserPO::getId,
                                UserPO::getUsername,
                                UserPO::getAuthorities,
                                UserPO::getPassword,
                                UserPO::isExpired,
                                UserPO::isStatus,
                                UserPO::isLocked,
                                UserPO::isCredentialsExpired)
                        .eq(UserPO::getUsername, username)
        );
        log.debug(JSON.toJSONString(userPO));
        if (userPO == null) {
            throw new UsernameNotFoundException("Username or password is incorrect!");
        }
        if (!userPO.isEnabled()) {
            throw new DisabledException("This account has been disabled!");
        } else if (!userPO.isAccountNonLocked()) {
            throw new LockedException("This account has been locked!");
        } else if (!userPO.isAccountNonExpired()) {
            throw new AccountExpiredException("This account has expired!");
        } else if (!userPO.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("This account credentials has expired!");
        }
        return userPO;
    }

}
