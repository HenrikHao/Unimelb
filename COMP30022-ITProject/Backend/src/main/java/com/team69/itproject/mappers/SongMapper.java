package com.team69.itproject.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.entities.dto.SongDTO;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.SongPO;

public interface SongMapper extends BaseMapper<SongPO> {

    Page<SongDTO> getSongList(Page<SongDTO> page);

    Page<SongDTO> getSongByCategory(Page<SongDTO> page, String category);

    Page<SongDTO> searchSongByName(Page<SongDTO> songPOPage, String name);

    Page<SongDTO> getUserSongListByName(Page<SongDTO> songPOPage, Long userId, String name);

}
