package com.team69.itproject;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.dao.SongDAO;
import com.team69.itproject.dao.UserDAO;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.UserPO;
import com.team69.itproject.services.UsersService;
import org.apache.logging.log4j.core.util.PasswordDecryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class UserDAOTests {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    @Rollback
    void testAddUser() {
        deleteAllUser();
        userDAO.addUser("Xuanniu", "aa@gmail.com", "123456789");
        UserPO userPO = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, "Xuanniu")
        );
        assertArrayEquals(userPO.getUsername().toCharArray(), "Xuanniu".toCharArray());
    }

    @Test
    @Transactional
    @Rollback
    void testGetUserList() {
        deleteAllUser();
        ArrayList<String> names = new ArrayList<>();
        int n = 20;
        for (Integer i = 0; i < n; i++) {
            names.add("Xuanniu" + i);
            UserPO newUser = UserPO.builder()
                    .username("Xuanniu" + i)
                    .email("aaa@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .authorities(List.of(new SimpleGrantedAuthority("normal")))
                    .build();
            usersService.save(newUser);
        }
        Page<UsersDTO> pages = userDAO.getUserList(1, 20);
        for (int i = 0; i < n; i++) {
            //UsersDTO xn0 = userDAO.getUserByUsername("Xuanniu0");
            assertArrayEquals(pages.getRecords().get(i).getUsername().toCharArray(), names.get(i).toCharArray());
        }
    }

    @Test
    @Transactional
    @Rollback
    void testGetUserByUsername() {
        deleteAllUser();
        UserPO newUser = UserPO.builder()
                .username("Xuanniu")
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        UsersDTO xn = userDAO.getUserByUsername("Xuanniu");
        assert(xn.getUsername().equals("Xuanniu"));
    }

    @Test
    @Transactional
    @Rollback
    void testGetUserById() {
        String name = "Xuanniu";
        deleteAllUser();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );
        long id = xn.getId();
        UsersDTO xnn = userDAO.getUserById(id);
        assertArrayEquals(xnn.getUsername().toCharArray(),name.toCharArray());
        long fid = id +1;
        UsersDTO failCase = userDAO.getUserById(fid);
        assert(failCase.getUsername()==null);
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateUserEmail() {
        String name = "Xuanniu";
        String email = "bbb@outlook.com";
        deleteAllUser();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );
        long id = xn.getId();
        userDAO.updateUserEmail(id,email);
        xn = usersService.getById(id);
        assertArrayEquals(xn.getEmail().toCharArray(),email.toCharArray());
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateUserPassword() {
        String name = "Xuanniu";
        String password = "cnji257856f";
        String encodedPassword = passwordEncoder.encode(password);
        deleteAllUser();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );
        long id = xn.getId();
        userDAO.updateUserPassword(id,encodedPassword);
        UserPO xn1 = usersService.getById(id);
        assert(!xn1.getPassword().equals(xn.getPassword()));
    }

    void deleteAllUser(){
        usersService.remove(new QueryWrapper<>());
    }

}
