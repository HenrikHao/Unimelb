package com.team69.itproject;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.team69.itproject.entities.dto.SongDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team69.itproject.dao.SongDAO;
import com.team69.itproject.dao.UserDAO;
import com.team69.itproject.entities.vo.SongVO;
import org.springframework.http.MediaType;
import com.team69.itproject.entities.vo.RegisterVO;
import com.team69.itproject.entities.dto.UsersDTO;

@SpringBootTest
@ActiveProfiles("test")
class ItProjectApplicationTests {
    public class SongControllerTests {
        private MockMvc mockMvc;
        private SongDAO songDAO = mock(SongDAO.class);

        @Test
        public void testGetSongList() throws Exception {
            Page<SongDTO> mockPage = mock(Page.class);
            when(songDAO.getSongList(anyInt(), anyInt())).thenReturn(mockPage);

            mockMvc.perform(get("/song/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        public void testGetSongByCategory() throws Exception {
            Page<SongDTO> mockPage = mock(Page.class);
            when(songDAO.getSongByCategory(anyInt(), anyInt(), anyString())).thenReturn(mockPage);

            mockMvc.perform(get("/song/list/category")
                            .param("page", "1")
                            .param("size", "10")
                            .param("category", "testCategory"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        public void testGetSongById() throws Exception {
            SongDTO mockSong = mock(SongDTO.class);
            when(songDAO.getSongById(anyLong())).thenReturn(mockSong);

            mockMvc.perform(get("/song/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }

        public void testAddSong() throws Exception {
            when(songDAO.addSong(any())).thenReturn(true);

            SongVO songVO = new SongVO();
            mockMvc.perform(post("/song/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(songVO)))
                    .andExpect(status().isOk());
        }

        public void testAddSong_NoAdminRights() throws Exception {
            mockMvc.perform(post("/song/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(new SongVO())))
                    .andExpect(status().isForbidden());
        }

        public static String asJsonString(final Object obj) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public class UserControllerTests {
        private MockMvc mockMvc;

        private UserDAO userDAO;
        public void testRegister() throws Exception {
            RegisterVO registerVO = new RegisterVO();
            registerVO.setUsername("testuser");
            registerVO.setEmail("testuser@email.com");
            registerVO.setPassword("testpassword");

            mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(registerVO)))
                    .andExpect(status().isOk());
        }
        public void testGetUserByUsername() throws Exception {
            String testUsername = "testuser";
            UsersDTO mockUser = new UsersDTO();
            when(userDAO.getUserByUsername(testUsername)).thenReturn(mockUser);

            mockMvc.perform(get("/user/{username}", testUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }
        public void testGetUserList() throws Exception {
            Page<UsersDTO> mockPage = mock(Page.class);
            when(userDAO.getUserList(anyInt(), anyInt())).thenReturn(mockPage);

            mockMvc.perform(get("/user/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").exists());
        }
        public static String asJsonString(final Object obj) {
            try {
                return new ObjectMapper().writeValueAsString(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}