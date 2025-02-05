package com.team69.itproject.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName user_song_list
 */
@TableName(value = "user_song_list")
@Data
public class UserSongListPO implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     *
     */
    @TableField(value = "user_id")
    private Long userId;
    /**
     *
     */
    @TableField(value = "song_id")
    private Long songId;
    /**
     *
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
    /**
     *
     */
    @TableField(value = "name")
    private String name;
}
