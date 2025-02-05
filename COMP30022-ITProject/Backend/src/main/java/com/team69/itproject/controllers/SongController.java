package com.team69.itproject.controllers;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team69.itproject.aop.annotations.UserAuth;
import com.team69.itproject.aop.enums.AccessLevel;
import com.team69.itproject.dao.SongDAO;
import com.team69.itproject.entities.bo.ResponseEntity;
import com.team69.itproject.entities.dto.SongDTO;
import com.team69.itproject.entities.vo.AddSongToListVO;
import com.team69.itproject.entities.vo.SongVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Api(tags = "Song APIs")
@RestController
@RequestMapping("/song")
public class SongController {
    private final List<String> listName = Arrays.asList("Background", "Sleep", "Relax", "Favourite");
    @Resource
    private SongDAO songDAO;

    @ApiOperation(
            value = "Get song list",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    public ResponseEntity<Page<SongDTO>> getSongList(@RequestParam("page") int page,
                                                     @RequestParam("size") int size) {
        Page<SongDTO> songList = songDAO.getSongList(page, size);
        if (songList == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(songList);
    }

    @ApiOperation(
            value = "Search song by name",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/search/name")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    public ResponseEntity<Page<SongDTO>> getSongByName(@RequestParam("page") int page,
                                                       @RequestParam("size") int size,
                                                       @RequestParam("name") String name) {
        Page<SongDTO> songList = songDAO.getSongByName(page, size, name);
        if (songList == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(songList);
    }

    @ApiOperation(
            value = "Get song by category",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/list/category")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    public ResponseEntity<Page<SongDTO>> getSongByCategory(@RequestParam("page") int page,
                                                           @RequestParam("size") int size,
                                                           @RequestParam("category") String category) {
        Page<SongDTO> songList = songDAO.getSongByCategory(page, size, category);
        if (songList == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(songList);
    }

    @ApiOperation(
            value = "Get song by id",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    public ResponseEntity<SongDTO> getSongById(@PathVariable("id") Long id) {
        SongDTO songById = songDAO.getSongById(id);
        if (songById == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(songById);
    }

    @ApiOperation(
            value = "Add a song",
            authorizations = {@Authorization("admin")}
    )
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<String> addSong(@RequestBody @Valid SongVO songVO) {
        boolean addSong = songDAO.addSong(songVO);
        if (addSong) {
            return ResponseEntity.ok();
        }
        return ResponseEntity.error(501, "Add song failed");
    }

    @ApiOperation(
            value = "Update a song",
            authorizations = {@Authorization("admin")}
    )
    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<String> updateSong(@PathVariable("id") Long id,
                                             @RequestBody @Valid SongVO songVO) {
        boolean updateSong = songDAO.updateSong(id, songVO);
        if (updateSong) {
            return ResponseEntity.ok();
        }
        return ResponseEntity.error(501, "Update song failed");
    }

    @ApiOperation(
            value = "Delete a song",
            authorizations = {@Authorization("admin")}
    )
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<String> deleteSong(@PathVariable("id") Long id) {
        boolean deleteSong = songDAO.deleteSong(id);
        if (deleteSong) {
            return ResponseEntity.ok();
        }
        return ResponseEntity.error(501, "Delete song failed");
    }

    @ApiOperation(
            value = "Get a user's song list",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @PostMapping("/userSongList")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<Page<SongDTO>> getUserSongList(@RequestHeader("User-Id") Long id,
                                                         @RequestParam("page") int page,
                                                         @RequestParam("size") int size,
                                                         @RequestParam("songListName") String songListName) {
        Page<SongDTO> songList = songDAO.getUserSongListByName(id, songListName, page, size);
        if (songList == null) {
            return ResponseEntity.error(404, null);
        }
        return ResponseEntity.ok(songList);
    }

    @ApiOperation(
            value = "Add a song to user's song list",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @PostMapping("/addSongToList")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<String> addSongToList(@RequestHeader("User-Id") Long userId,
                                                @RequestBody @Valid AddSongToListVO addSongToListVO) {
        String songListName = addSongToListVO.getSongListName();
        if (!listName.contains(songListName)) {
            return ResponseEntity.error(501, "Song list name is not allowed");
        }
        songDAO.addSongToUserSongList(userId, songListName, addSongToListVO.getSongIdList());
        return ResponseEntity.ok();
    }

    @ApiOperation(
            value = "Delete a song from user's song list",
            authorizations = {@Authorization("normal"), @Authorization("admin")}
    )
    @PostMapping("/deleteSongFromList")
    @PreAuthorize("hasAnyAuthority('admin', 'normal')")
    @UserAuth({AccessLevel.SELF, AccessLevel.ADMIN})
    public ResponseEntity<String> deleteSongFromList(@RequestHeader("User-Id") Long userId,
                                                     @RequestBody @Valid AddSongToListVO addSongToListVO) {
        String songListName = addSongToListVO.getSongListName();
        if (!listName.contains(songListName)) {
            return ResponseEntity.error(501, "Song list name is not allowed");
        }
        songDAO.removeSongFromUserSongList(userId, songListName, addSongToListVO.getSongIdList());
        return ResponseEntity.ok();
    }
}
