package antessio.personalfinance.infrastructure.web.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RequestLoggingFilterConfig {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilterConfig.class);

    @Bean
    public Filter requestLoggingFilter() {
        return (ServletRequest request, ServletResponse response, FilterChain chain) -> {
            if (request instanceof HttpServletRequest req) {
                long start = System.currentTimeMillis();
                String method = req.getMethod();
                String uri = req.getRequestURI();
                String query = req.getQueryString();
                logger.info("HTTP {} {}{}", method, uri, query != null ? "?" + query : "");
                chain.doFilter(request, response);
                long duration = System.currentTimeMillis() - start;
                logger.info("Completed {} {} in {} ms", method, uri, duration);
            } else {
                chain.doFilter(request, response);
            }
        };
    }
}