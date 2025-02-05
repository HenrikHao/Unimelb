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
class SongDAOTests {

    @Autowired
    private SongDAO songDAO;
    @Autowired
    private SongService songService;
    @Autowired
    private UserSongListService USLService;
    @Autowired
    private UserSongListService UsersService;

    @Test
    @Transactional
    @Rollback
    void testGetSongById() {
        deleteAllSong();
        String songName = "ABC";
        SongVO songVO = new SongVO(songName, "aaa", "DEF", LocalDate.now());
        SongPO newSong = new SongPO();
        BeanUtil.copyProperties(songVO, newSong);
        songService.save(newSong);

        Page<SongDTO> songPOPage = new Page<>(0, 20);
        Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName);
        long id = songs.getRecords().get(0).getId();

        SongDTO song = songDAO.getSongById(id);
        assert (song.getName().equals(songName));
    }

    @Test
    @Transactional
    @Rollback
    void testGetSongList() {
        deleteAllSong();
        String songName = "ABC";
        int n = 20;
        for (Integer i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }

        Page<SongDTO> songPOPage = new Page<>(0, 20);
        Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName);
        long id = songs.getRecords().get(0).getId();

        Page<SongDTO> song = songDAO.getSongList(0, 20);
        for (int i = 0; i < n; i++) {
            assert (song.getRecords().get(i).getName().equals(songName + i));
        }
    }

    @Test
    @Transactional
    @Rollback
    void testAddSong() {
        deleteAllSong();
        String songName = "ABC";
        String songAuthor = "DEF";
        int n = 20;
        SongVO songVO = new SongVO(songName, "aaa", songAuthor, LocalDate.now());
        songDAO.addSong(songVO);


        Page<SongDTO> songPOPage = new Page<>(0, 20);
        Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName);
        long id = songs.getRecords().get(0).getId();
        assert (songs.getRecords().get(0).getAuthor().equals(songAuthor));
    }

    @Test
    @Transactional
    @Rollback
    void testUpdateSong() {
        deleteAllSong();
        String songName = "ABC";
        int n = 20;
        for (Integer i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }

        Page<SongDTO> songPOPage = new Page<>(0, 20);
        Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+5);
        long id = songs.getRecords().get(0).getId();

        SongVO songVO = new SongVO("ASDAD", "asd", "fff", LocalDate.now());
        songDAO.updateSong(id,songVO);

        songs = songService.searchSongByName(songPOPage,"ASDAD");
        assert(songs.getRecords().get(0).getId().equals(id));
    }

    void deleteAllSong(){
        songService.remove(new QueryWrapper<>());
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteSong() {
        deleteAllSong();
        String songName = "ABC";
        int n = 20;
        for (Integer i = 0; i < n; i++) {
            SongVO songVO = new SongVO(songName + i, "aaa", "DEF", LocalDate.now());
            SongPO newSong = new SongPO();
            BeanUtil.copyProperties(songVO, newSong);
            songService.save(newSong);
        }

        Page<SongDTO> songPOPage = new Page<>(0, 20);
        Page<SongDTO> songs = songService.searchSongByName(songPOPage, songName+5);
        long id = songs.getRecords().get(0).getId();

        songDAO.deleteSong(id);
        SongPO song = songService.getById(id);
        assert(song == null);

    }

    @Test
    @Transactional
    @Rollback
    void testGetSongByName() {
        deleteAllSong();
        String songName = "ABC";
        SongVO songVO = new SongVO(songName, "aaa", "DEF", LocalDate.now());
        SongPO newSong = new SongPO();
        BeanUtil.copyProperties(songVO, newSong);
        songService.save(newSong);

        Page<SongDTO> song = songDAO.getSongByName(0,20,songName);
        assert (song.getRecords().get(0).getName().equals(songName));
        song = songDAO.getSongByName(0,20,"CAB");
        assert (song.getTotal()==0);
    }


}
