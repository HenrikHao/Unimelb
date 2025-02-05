package com.team69.itproject.services.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.UserPO;
import com.team69.itproject.mappers.UsersMapper;
import com.team69.itproject.services.UsersService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @description 针对表【users】的数据库操作Service实现
 * @createDate 2022-11-02 17:24:26
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, UserPO>
        implements UsersService {
    @Resource
    private UsersMapper usersMapper;

    @Override
    public Page<UsersDTO> getUserList(Page<UsersDTO> page) {
        return usersMapper.getUserList(page);
    }

}




