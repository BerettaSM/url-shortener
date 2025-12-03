package com.ramon.urlshortener.domain.dtos;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ShortenedUrlDTO {

    @NotBlank(message = "Url must not be blank.")
    @URL(message = "You must provide a valid url.")
    private String url;

}
