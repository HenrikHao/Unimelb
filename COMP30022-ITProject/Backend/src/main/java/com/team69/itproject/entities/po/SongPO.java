package com.team69.itproject.entities.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @TableName songs
 */
@TableName(value = "songs", autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Song Persistent Object")
public class SongPO {

    @ApiModelProperty(value = "Song ID", notes = "ID of song, auto-generated")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "Song Name", notes = "Name of the song")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "Song Description", notes = "Brief description of the song")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty(value = "Song Author", notes = "Author of the song")
    @TableField(value = "author")
    private String author;

    @ApiModelProperty(value = "Song Release Date", notes = "Date when the song is firstly released")
    @TableField(value = "release_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @ApiModelProperty(value = "Song Create Time", notes = "Date when the song is created")
    @TableField(value = "create_time")
    @Builder.Default
    private LocalDate createTime = LocalDate.now();

    @ApiModelProperty(value = "Song state", notes = "0 for disable, 1 for enable")
    @TableLogic
    @TableField(value = "status")
    @Builder.Default
    private boolean status = true;

}
