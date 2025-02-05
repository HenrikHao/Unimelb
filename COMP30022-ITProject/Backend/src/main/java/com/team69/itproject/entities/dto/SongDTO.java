package com.team69.itproject.entities.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDTO implements Serializable {

    @ApiModelProperty(value ="Song ID", notes = "ID of song")
    private Long id;

    @ApiModelProperty(value ="Song Name", notes = "Name of song")
    private String name;

    @ApiModelProperty(value ="Song Description", notes = "Detail of the song, any text description")
    private String description;

    @ApiModelProperty(value ="Song Author", notes = "Creater of the song")
    private String author;

    @ApiModelProperty(value ="Song Release Date", notes = "First time release date of the song")
    private LocalDate releaseDate;

    @ApiModelProperty(value ="Song Create Time", notes = "Date when the song is created")
    @Builder.Default
    private LocalDate createTime = LocalDate.now();

    @ApiModelProperty(value = "Song state", notes = "0 for disable, 1 for enable")
    @Builder.Default
    private boolean status = true;
}
