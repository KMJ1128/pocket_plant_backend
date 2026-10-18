package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PasswordResetControllerTest {
    private final PasswordResetService service = mock(PasswordResetService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PasswordResetController(service))
            .setControllerAdvice(new ApiExceptionHandler()).build();

    @Test
    void jsonEndpointsMatchTheMobileContract() throws Exception {
        mvc.perform(post("/api/email/password-reset/request").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"plant@example.com\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/email/password-reset/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"plant@example.com\",\"code\":\"123456\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/email/password-reset/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"plant@example.com\",\"code\":\"123456\",\"newPassword\":\"NewPass2!\"}"))
                .andExpect(status().isOk());
        verify(service).requestCode("plant@example.com");
        verify(service).verifyCode("plant@example.com", "123456");
        verify(service).resetPassword("plant@example.com", "123456", "NewPass2!");
    }

    @Test
    void invalidInputAndExpiredCodeAreRejected() throws Exception {
        mvc.perform(post("/api/email/password-reset/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad\",\"code\":\"1\",\"newPassword\":\"x\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
        doThrow(new IllegalArgumentException("expired")).when(service).verifyCode("plant@example.com", "123456");
        mvc.perform(post("/api/email/password-reset/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"plant@example.com\",\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("expired"));
    }
}
