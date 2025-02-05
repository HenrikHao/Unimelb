package com.team69.itproject.services;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.team69.itproject.entities.dto.SongDTO;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.SongPO;

public interface SongService extends IService<SongPO> {
    Page<SongDTO> getSongList(Page<SongDTO> songPOPage);

    Page<SongDTO> getSongByCategory(Page<SongDTO> songPOPage, String category);

    Page<SongDTO> searchSongByName(Page<SongDTO> songPOPage, String name);

    Page<SongDTO> getUserSongListByName(Page<SongDTO> songPOPage, Long userId, String name);
}
