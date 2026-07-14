package com.rahul.quoteplatform.quoteapi.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rahul.quoteplatform.quoteapi.application.CreateQuoteResponse;
import com.rahul.quoteplatform.quoteapi.application.QuoteApplicationService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuoteApplicationService quoteApplicationService;

    @Test
    void shouldAcceptQuoteCreationRequest() throws Exception {
        when(quoteApplicationService.createQuote(any())).thenReturn(new CreateQuoteResponse(UUID.fromString("11111111-1111-1111-1111-111111111111")));

        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "cust-123",
                                  "customerEmail": "customer@example.com",
                                  "requestedAmount": 125.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quoteId").value("11111111-1111-1111-1111-111111111111"));
    }
}
