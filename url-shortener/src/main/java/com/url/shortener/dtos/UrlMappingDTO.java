package com.url.shortener.dtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UrlMappingDTO {

  private Long id;
    private String originalUrl;
    private String shortUrl;
    private int clickCount;
    private LocalDate createdDate;
    private String username; // Add username to track which user created the URL mapping



}
