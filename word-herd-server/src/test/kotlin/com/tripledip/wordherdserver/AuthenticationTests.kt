package com.tripledip.wordherdserver

import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationTests {

    @Autowired
    lateinit var mockMvc: MockMvc


    @Test
    fun anonymousMayNotCheckAuth() {
        mockMvc.perform(get("/checkAuth"))
            .andExpect(status().is3xxRedirection)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @WithMockUser("mock-user")
    fun authenticatedUserMayCheckAuth() {
        mockMvc.perform(get("/checkAuth"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("If you can read this, you are authenticated.")))
    }

    @Test
    fun anonymousMayNotUpgradeToWebsocket() {
        mockMvc.perform(get("/words"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithMockUser("mock-user")
    fun authenticatedUserMayUpgradeToWebsocket() {
        mockMvc.perform(get("/words"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Welcome to SockJS!")))
    }
}
