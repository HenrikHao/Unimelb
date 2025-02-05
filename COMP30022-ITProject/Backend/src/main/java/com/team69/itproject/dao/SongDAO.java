package com.team69.itproject.dao;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.entities.dto.SongDTO;
import com.team69.itproject.entities.po.SongPO;
import com.team69.itproject.entities.po.UserSongListPO;
import com.team69.itproject.entities.vo.SongVO;
import com.team69.itproject.services.SongService;
import com.team69.itproject.services.UserSongListService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SongDAO {
    @Resource
    private SongService songService;
    @Resource
    private UserSongListService userSongListService;

    @Cacheable(value = "song", key = "#id")
    public SongDTO getSongById(Long id) {
        SongPO songPO = songService.getById(id);
        SongDTO songDTO = new SongDTO();
        BeanUtil.copyProperties(songPO, songDTO);
        return songDTO;
    }

    @Cacheable(value = "songList", key = "#page+'-'+#size")
    public Page<SongDTO> getSongList(int page, int size) {
        Page<SongDTO> songPOPage = new Page<>(page, size);
        return songService.getSongList(songPOPage);
    }

    @Cacheable(value = "songList", key = "#category+'-'+#page+'-'+#size")
    public Page<SongDTO> getSongByCategory(int page, int size, String category) {
        Page<SongDTO> songPOPage = new Page<>(page, size);
        return songService.getSongByCategory(songPOPage, category);
    }

    @CacheEvict(value = "songList", allEntries = true)
    public boolean addSong(SongVO songVO) {
        SongPO newSong = new SongPO();
        BeanUtil.copyProperties(songVO, newSong);
        return songService.save(newSong);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "songList", allEntries = true),
                    @CacheEvict(value = "song", key = "#id"),
                    @CacheEvict(value = "userSongList", allEntries = true)
            }
    )
    public boolean updateSong(Long id, SongVO songVO) {
        SongPO songPO = songService.getById(id);
        BeanUtil.copyProperties(songVO, songPO);
        return songService.saveOrUpdate(songPO);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "songList", allEntries = true),
                    @CacheEvict(value = "song", key = "#id"),
                    @CacheEvict(value = "userSongList", allEntries = true)
            }
    )
    public boolean deleteSong(Long id) {
        return songService.removeById(id);
    }

    @Cacheable(value = "songList", key = "#name+'-'+#page+'-'+#size")
    public Page<SongDTO> getSongByName(int page, int size, String name) {
        Page<SongDTO> songPOPage = new Page<>(page, size);
        return songService.searchSongByName(songPOPage, name);
    }

    @Cacheable(value = "userSongList", key = "#userId+'-'+#name+'-'+#page+'-'+#size")
    public Page<SongDTO> getUserSongListByName(Long userId, String name, int page, int size) {
        Page<SongDTO> songPOPage = new Page<>(page, size);
        return songService.getUserSongListByName(songPOPage, userId, name);
    }

    @CacheEvict(value = "userSongList", allEntries = true)
    public void addSongToUserSongList(Long userId, String listName, List<Long> songIds) {
        for (Long songId : songIds) {
            if (isSongExistsInUserList(userId, listName, songId)) continue;
            UserSongListPO userSongListPO = new UserSongListPO();
            userSongListPO.setUserId(userId);
            userSongListPO.setSongId(songId);
            userSongListPO.setName(listName);
            userSongListService.save(userSongListPO);
        }
    }

    @CacheEvict(value = "userSongList", allEntries = true)
    public void removeSongFromUserSongList(Long userId, String listName, List<Long> songIds) {
        for (Long songId : songIds) {
            if (!isSongExistsInUserList(userId, listName, songId)) continue;
            userSongListService.remove(
                    new LambdaQueryWrapper<UserSongListPO>()
                            .eq(UserSongListPO::getSongId, songId)
                            .and(wrapper -> wrapper.eq(UserSongListPO::getUserId, userId)
                                    .and(
                                            wrapper1 -> wrapper1.eq(UserSongListPO::getName, listName)
                                    )
                            )
            );
        }
    }

    private boolean isSongExistsInUserList(Long userId, String listName, Long songId) {
        return userSongListService.exists(
                new LambdaQueryWrapper<UserSongListPO>()
                        .eq(UserSongListPO::getSongId, songId)
                        .and(wrapper -> wrapper.eq(UserSongListPO::getUserId, userId)
                                .and(
                                        wrapper1 -> wrapper1.eq(UserSongListPO::getName, listName)
                                )
                        )
        );
    }
}
