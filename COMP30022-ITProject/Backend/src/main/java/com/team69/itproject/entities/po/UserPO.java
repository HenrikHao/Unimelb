package com.team69.itproject.entities.po;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;
import com.team69.itproject.handlers.SimpleGrantTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @TableName users
 */
@TableName(value = "users", autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("User Persistent Object")
public class UserPO implements Serializable, UserDetails {
    @TableField(exist = false)
    @ApiModelProperty(hidden = true)
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * User ID
     */
    @ApiModelProperty(value ="User ID", notes = "Auto-generated user id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * Username
     * Unique, used for login
     */
    @ApiModelProperty(value = "Username", notes = "Unique, used for login")
    @TableField(value = "username")
    private String username;
    /**
     * User password
     */
    @ApiModelProperty(value ="User password", notes = "Password of user")
    @TableField(value = "password")
    private String password;
    /**
     * User Email
     */
    @ApiModelProperty(value ="User Email", notes = "User's email")
    @TableField(value = "email")
    private String email;
    /**
     * Account state
     * 0 for disable, 1 for enable
     */
    @ApiModelProperty(value = "Account state", notes = "0 for disable, 1 for enable")
    @TableLogic
    @TableField(value = "status")
    @Builder.Default
    private boolean status = true;
    /**
     * Created time
     */
    @ApiModelProperty(value ="Created time", notes ="Date when user is created")
    @TableField(value = "create_time")
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    /**
     * User remark
     */
    @ApiModelProperty(value ="User remark", notes = "Some information about user")
    @TableField(value = "remark")
    private String remark = "";
    /**
     * If the account is expired
     */
    @ApiModelProperty(value ="If the account is expired")
    @TableField(value = "expired")
    @Builder.Default
    private boolean expired = false;
    /**
     * If the account is locked
     */
    @ApiModelProperty(value ="If the account is locked")
    @TableField(value = "locked")
    @Builder.Default
    private boolean locked = false;
    /**
     * If the credentials is expired
     */
    @ApiModelProperty(value ="If the credentials is expired")
    @TableField(value = "credentials_expired")
    @Builder.Default
    private boolean credentialsExpired = false;
    /**
     * User authorities
     */
    @ApiModelProperty(value ="User authorities", notes = "The user's authorities")
    @TableField(value = "authorities", typeHandler = SimpleGrantTypeHandler.class)
    private List<SimpleGrantedAuthority> authorities;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return status;
    }
}
