package com.team69.itproject.controllers;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.aop.annotations.UserAuth;
import com.team69.itproject.aop.enums.AccessLevel;
import com.team69.itproject.dao.UserDAO;
import com.team69.itproject.entities.bo.ResponseEntity;
import com.team69.itproject.entities.dto.UsersDTO;
import com.team69.itproject.entities.vo.RegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "User APIs")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserDAO userDAO;

    @ApiOperation("Register a new user")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterVO registerVO) {
        boolean registerSucceed = userDAO.addUser(
                registerVO.getUsername(),
                registerVO.getEmail(),
                registerVO.getPassword()
        );
        if (registerSucceed) {
            return ResponseEntity.ok();
        }
        return ResponseEntity.error(501, "Register failed");
    }

    @ApiOperation(
            value = "Check username availability",
            authorizations = {}
    )
    @GetMapping("/check/{username}")
    public ResponseEntity<Boolean> checkUsernameAvailability(@PathVariable("username") String username) {
        return ResponseEntity.ok(userDAO.getUserByUsername(username) == null);
    }

    @ApiOperation(
            value = "Get a user by username",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/{username}")
    @PreAuthorize("hasAnyAuthority('normal', 'admin')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<UsersDTO> getUserByUsername(@PathVariable("username") String username) {
        UsersDTO userByUsername = userDAO.getUserByUsername(username);
        if (userByUsername == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(userByUsername);
    }

    @ApiOperation(
            value = "Get a user by id",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('normal', 'admin')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<UsersDTO> getUserById(@RequestHeader("User-Id") Long userId) {
        UsersDTO userById = userDAO.getUserById(userId);
        if (userById == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(userById);
    }

    @ApiOperation(
            value = "Get user list",
            authorizations = {@Authorization("admin")}
    )
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<Page<UsersDTO>> getUserList(@RequestParam("page") int page,
                                                      @RequestParam("size") int size) {
        Page<UsersDTO> userList = userDAO.getUserList(page, size);
        if (userList == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(userList);
    }

    @ApiOperation(
            value = "Update User Email",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @PostMapping("/update/email")
    @PreAuthorize("hasAnyAuthority('normal', 'admin')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<String> updateEmail(@RequestHeader("User-Id") Long userId, @RequestBody Map<String, String> data) {
        UsersDTO userById = userDAO.getUserById(userId);
        if (userById == null) {
            return ResponseEntity.error(404, null);
        }
        userDAO.updateUserEmail(userId, data.get("email"));
        return ResponseEntity.ok();
    }

    @ApiOperation(
            value = "Update User Password",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @PostMapping("/update/password")
    @PreAuthorize("hasAnyAuthority('normal', 'admin')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<String> updatePassword(@RequestHeader("User-Id") Long userId, @RequestBody Map<String, String> data) {
        UsersDTO userById = userDAO.getUserById(userId);
        if (userById == null) {
            return ResponseEntity.error(404, null);
        }
        userDAO.updateUserPassword(userId, data.get("password"));
        return ResponseEntity.ok();
    }

    @ApiOperation(
            value = "Update User Authority",
            authorizations = {@Authorization("admin")}
    )
    @PostMapping("/update/authority")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<String> updateAuthority(@RequestParam("userId") Long userId, @RequestBody Map<String, String> data) {
        UsersDTO userById = userDAO.getUserById(userId);
        if (userById == null) {
            return ResponseEntity.error(404, null);
        }
        userDAO.updateUserAuthority(userId, data.get("authority"));
        return ResponseEntity.ok();
    }
}
