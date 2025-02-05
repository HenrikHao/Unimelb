package com.team69.itproject.services.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.team69.itproject.entities.po.UserSongListPO;
import com.team69.itproject.mappers.UserSongListMapper;
import com.team69.itproject.services.UserSongListService;
import org.springframework.stereotype.Service;

/**
 * @author Ling Bao
 * @description 针对表【user_song_list】的数据库操作Service实现
 * @createDate 2023-09-11 16:41:33
 */
@Service
public class UserSongListServiceImpl extends ServiceImpl<UserSongListMapper, UserSongListPO>
        implements UserSongListService {

}




