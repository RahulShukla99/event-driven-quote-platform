package com.rahul.quoteplatform.quoteapi.web;

import com.rahul.quoteplatform.quoteapi.application.CreateQuoteCommand;
import com.rahul.quoteplatform.quoteapi.application.QuoteApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    private final QuoteApplicationService quoteApplicationService;

    public QuoteController(QuoteApplicationService quoteApplicationService) {
        this.quoteApplicationService = quoteApplicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public com.rahul.quoteplatform.quoteapi.application.CreateQuoteResponse createQuote(@RequestBody CreateQuoteRequest request) {
        return quoteApplicationService.createQuote(new CreateQuoteCommand(
                request.customerId(),
                request.customerEmail(),
                request.requestedAmount()));
    }
}
