package com.okits02.SpringJWTWithOauth2.repository.Oauth2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.okits02.SpringJWTWithOauth2.dto.GoogleUserDTO;

@FeignClient(name = "google-userinfo", url = "https://www.googleapis.com")
public interface GoogleUserInfo {
    @GetMapping(value = "/oauth2/v1/userinfo")
    GoogleUserDTO getUserInfo(@RequestParam("alt") String alt, @RequestParam("access_token") String accessToken);
}
