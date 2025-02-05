package com.team69.itproject.entities.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Song Value Object")
public class SongVO {

    @ApiModelProperty(value="Song Name", notes = "Name of the song in question")
    @NotEmpty(message = "Song name should not be empty!")
    private String name;

    @ApiModelProperty(value="Song Description", notes ="Text description of the song")
    @NotEmpty(message = "Song description should not be empty!")
    private String description;

    @ApiModelProperty(value="Song Author", notes ="Author of the song")
    @NotEmpty(message = "Song author should not be empty!")
    private String author;

    @ApiModelProperty(value="Song Release Date", notes = "Date when the song is released")
    @NotNull(message = "Song release date should not be empty!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
}
