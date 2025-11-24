package com.anki.simple.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {
  private String type;
  private String title;
  private int status;
  private String detail;
  private String instance;
  private LocalDateTime timestamp;
}
