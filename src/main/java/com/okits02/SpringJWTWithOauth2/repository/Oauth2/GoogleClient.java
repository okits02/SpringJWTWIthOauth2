package com.okits02.SpringJWTWithOauth2.repository.Oauth2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import com.okits02.SpringJWTWithOauth2.dto.response.Oauth2GoogleResponse;
import com.okits02.SpringJWTWithOauth2.dto.resquest.Oauth2GoogleRequest;

import feign.QueryMap;

@FeignClient(name = "oauth2-google", url = "https://oauth2.googleapis.com")
public interface GoogleClient {
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Oauth2GoogleResponse exchangeToken(@QueryMap Oauth2GoogleRequest oauth2GoogleRequest);
}
