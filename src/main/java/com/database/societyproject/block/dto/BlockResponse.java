package com.database.societyproject.block.dto;

import java.time.LocalDateTime;
public record BlockResponse(Long id, String name, Integer totalFloors, Integer totalFlats,
                            Boolean active, LocalDateTime createdAt, Long societyId) { }
