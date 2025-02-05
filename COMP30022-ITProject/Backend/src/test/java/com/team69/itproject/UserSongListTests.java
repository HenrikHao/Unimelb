package com.team69.itproject;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.dao.SongDAO;
import com.team69.itproject.dao.UserDAO;
import com.team69.itproject.entities.dto.SongDTO;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.SongPO;
import com.team69.itproject.entities.po.UserPO;
import com.team69.itproject.entities.po.UserSongListPO;
import com.team69.itproject.entities.vo.SongVO;
import com.team69.itproject.mappers.SongMapper;
import com.team69.itproject.services.SongService;
import com.team69.itproject.services.UserSongListService;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
class UserSongListTests {

    @Autowired
    private SongDAO songDAO;
    @Autowired
    private SongService songService;
    @Autowired
    private UserSongListService USLService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    void deleteAllSong(){
        songService.remove(new QueryWrapper<>());
    }

    void deleteAllUser(){
        usersService.remove(new QueryWrapper<>());
    }

    @Test
    @Transactional
    @Rollback
    void testAddSongToUserSongList() {
        String name = "XNXNXN";
        deleteAllUser();
        deleteAllSong();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        String songName = "ABC";
        int n = 20;
        int m=4;
        for (int i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            Page<SongDTO> songPOPage = new Page<>(0, 20);
            Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+i);
            long id = songs.getRecords().get(0).getId();
            ids.add(id);
        }

        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );

        //
        songDAO.addSongToUserSongList(xn.getId(),"Favorate",ids);
        Page<SongDTO> songPOPage = new Page<>(0, 20);
        songService.getUserSongListByName(songPOPage, xn.getId(), "Favorate");
        for (int i = 0; i < m; i++) {
            assert (songPOPage.getRecords().get(i).getName().equals(songName+i));
        }

    }

    @Test
    @Transactional
    @Rollback
    void testGetUserSongListByName() {
        String name = "XNXNXN";
        deleteAllUser();
        deleteAllSong();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        String songName = "ABC";
        int n = 20;
        int m=4;
        for (int i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }

        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );

        for (int i = 0; i < m; i++) {
            Page<SongDTO> songPOPage = new Page<>(0, 20);
            Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+i);
            long id = songs.getRecords().get(0).getId();
            UserSongListPO userSongListPO = new UserSongListPO();
            userSongListPO.setUserId(xn.getId());
            userSongListPO.setSongId(id);
            userSongListPO.setName("Favorate");
            USLService.save(userSongListPO);
        }


        //
        Page<SongDTO> songPOPage = songDAO.getUserSongListByName(xn.getId(),"Favorate",0,20);
        for (int i = 0; i < m; i++) {
            assert (songPOPage.getRecords().get(i).getName().equals(songName+i));
        }

    }

    @Test
    @Transactional
    @Rollback
    void testRemoveSongFromUserSongList() {
        String name = "XNXNXN";
        deleteAllUser();
        deleteAllSong();
        UserPO newUser = UserPO.builder()
                .username(name)
                .email("aaa@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .authorities(List.of(new SimpleGrantedAuthority("normal")))
                .build();
        usersService.save(newUser);
        String songName = "ABC";
        int n = 20;
        int m=10;
        int k = 5;
        for (int i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }
        UserPO xn = usersService.getOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, name)
        );

        for (int i = 0; i < m; i++) {
            Page<SongDTO> songPOPage = new Page<>(0, 20);
            Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+i);
            long id = songs.getRecords().get(0).getId();
            UserSongListPO userSongListPO = new UserSongListPO();
            userSongListPO.setUserId(xn.getId());
            userSongListPO.setSongId(id);
            userSongListPO.setName("Favorate");
            USLService.save(userSongListPO);
        }

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Page<SongDTO> songPOPage = new Page<>(0, 20);
            Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+i);
            long id = songs.getRecords().get(0).getId();
            ids.add(id);
        }
        songDAO.removeSongFromUserSongList(xn.getId(),"Favorate",ids);

        Page<SongDTO> songPOPage = new Page<>(0, 20);
        songService.getUserSongListByName(songPOPage, xn.getId(), "Favorate");
        for (int i = 0; i < m-k; i++) {
            assert (songPOPage.getRecords().get(i).getName().equals(songName+(k+i)));;
            assert (!songPOPage.getRecords().get(i).getName().equals(songName+(0)));
        }

    }

}
