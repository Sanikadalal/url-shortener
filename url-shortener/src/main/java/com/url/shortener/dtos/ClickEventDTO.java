package com.url.shortener.dtos;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ClickEventDTO {

  private LocalDate clickDate;
  private Long clickCount;

}
