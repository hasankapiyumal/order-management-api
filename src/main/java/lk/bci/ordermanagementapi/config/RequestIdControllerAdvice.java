package lk.bci.ordermanagementapi.config;

import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

@ControllerAdvice
public class RequestIdControllerAdvice {

    @ModelAttribute
    public void populateRequestIdMdc(
            @RequestHeader(value = RequestIdFilter.REQUEST_ID_HEADER, required = false) String requestId
    ) {
        if (requestId != null && !requestId.isBlank()) {
            String trimmed = requestId.trim();
            MDC.put(RequestIdFilter.REQUEST_ID_HEADER, trimmed);
            MDC.put(RequestIdFilter.MDC_REQUEST_ID_KEY, trimmed);
        }
    }
}
