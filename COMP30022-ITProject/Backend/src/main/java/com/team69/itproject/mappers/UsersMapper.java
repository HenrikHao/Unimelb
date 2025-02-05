package com.team69.itproject.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.po.UserPO;

/**
 * @author Ling Bao
 * @description 针对表【users】的数据库操作Mapper
 * @createDate 2022-11-02 17:24:26
 * @Entity com.team69.itproject.entities.po.UserPO
 */
public interface UsersMapper extends BaseMapper<UserPO> {

    Page<UsersDTO> getUserList(Page<UsersDTO> page);
}




