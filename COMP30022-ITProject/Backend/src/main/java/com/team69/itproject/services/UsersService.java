package com.team69.itproject.services;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.UserPO;

/**
 * @author Ling Bao
 * @description 针对表【users】的数据库操作Service
 * @createDate 2022-11-02 17:24:26
 */
public interface UsersService extends IService<UserPO> {

    Page<UsersDTO> getUserList(Page<UsersDTO> page);
}
