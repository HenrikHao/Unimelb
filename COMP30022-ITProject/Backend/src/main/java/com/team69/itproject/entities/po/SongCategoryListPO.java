package com.team69.itproject.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @TableName song_category_list
 */
@TableName(value = "song_category_list")
@Data
public class SongCategoryListPO implements Serializable {
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
    @TableField(value = "category_id")
    private Long categoryId;
    /**
     *
     */
    @TableField(value = "song_id")
    private Long songId;
}
