package com.security_starter.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record PageRequestDTO(
		@Min(value = 0, message = "Page must be greater than or equal to 0")
        Integer page,

        @Min(value = 1, message = "Size must be greater than 0")
        @Max(value = 100, message = "Size must not exceed 100")
        Integer size,

        String sortBy,

        @Pattern(
                regexp = "ASC|DESC",
                message = "Direction must be ASC or DESC"
        )
        String direction
) 
{
	public PageRequestDTO {
        if (page == null) {
            page = 0;
        }

        if (size == null) {
            size = 10;
        }

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        if (direction == null || direction.isBlank()) {
            direction = "ASC";
        }
    }
}
