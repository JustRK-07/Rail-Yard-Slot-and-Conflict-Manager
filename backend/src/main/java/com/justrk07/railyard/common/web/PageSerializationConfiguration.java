package com.justrk07.railyard.common.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

/**
 * Configure Spring Data to serialize {@code Page} responses as a stable
 * DTO envelope. Without this, the default JSON shape of a Page is
 * undocumented and may change between Spring Boot releases.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class PageSerializationConfiguration {
}
