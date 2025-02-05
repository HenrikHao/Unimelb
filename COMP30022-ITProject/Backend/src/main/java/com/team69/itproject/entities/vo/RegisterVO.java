package com.team69.itproject.entities.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


@Data
@ApiModel("User register Value Object")
public class RegisterVO {
    @ApiModelProperty(value="Username, length between 1 and 30")
    @NotEmpty(message = "Username should not be empty!")
    @Length(min = 1, max = 30, message = "Invalid length")
    private String username;

    @ApiModelProperty(value ="Password, length between 6 and 128")
    @NotEmpty(message = "Password should not be empty!")
    @Length(min = 6, max = 128, message = "Invalid length")
    private String password;

    @ApiModelProperty(value="Email", notes = "User's email for account recovery")
    @NotEmpty(message = "Email should not be empty!")
    @Email
    private String email;
}
