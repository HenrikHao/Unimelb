package com.team69.itproject.entities.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("User Data Transfer Object")
public class UsersDTO implements Serializable {
    /**
     * User ID
     */
    @ApiModelProperty(value ="User ID", notes = "ID of user, auto-generated")
    private Long id;
    /**
     * Username
     * Unique, used for login
     */
    @ApiModelProperty(value = "Username", notes = "Unique username, used for login")
    private String username;
    /**
     * User email
     */
    @ApiModelProperty(value ="User Email", notes = "User Email for verification")
    private String email;
    /**
     * Account state
     * 0 for disable, 1 for enable
     */
    @ApiModelProperty(value = "Account state", notes = "0 for disable, 1 for enable")
    private boolean status;
    /**
     * Created time
     */
    @ApiModelProperty(value ="Created time", notes = "Date when the account is created")
    private LocalDateTime createTime;
    /**
     * User remark
     */
    @ApiModelProperty(value ="User remark")
    private String remark;
    /**
     * User authorities
     */
    @ApiModelProperty("User authorities")
    private List<SimpleGrantedAuthority> authorities;
}
