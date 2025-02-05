package com.team69.itproject.entities.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "Add song to list Value Object")
public class AddSongToListVO {
    @ApiModelProperty(value ="Song list name", notes = "The name of the list that is going to add a song")
    @NotEmpty(message = "Song list name should not be empty!")
    private String songListName;
    @ApiModelProperty(value ="A list of song ids", notes = "A list of song IDs that is going to be added to the target list")
    @NotNull(message = "Song id list should not be empty!")
    private List<Long> songIdList;
}
