package com.bank.deposit.calculation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateController.class)
class RateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_return_rate_and_interest() throws Exception {
        mockMvc.perform(get("/api/rates")
                        .param("term", "6")
                        .param("amount", "100000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratePercent").exists())
                .andExpect(jsonPath("$.estimatedInterest").exists());
    }
}

