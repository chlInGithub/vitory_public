package com.chl.victory.web.filter;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Component
public class MyEtagFilter extends ShallowEtagHeaderFilter {

    @Override
    protected boolean isEligibleForEtag(HttpServletRequest request, HttpServletResponse response,
            int responseStatusCode, InputStream inputStream) {
        return responseStatusCode == 304 || super.isEligibleForEtag(request, response, responseStatusCode, inputStream);
    }
}
