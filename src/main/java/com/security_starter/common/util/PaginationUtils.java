package com.security_starter.common.util;

import org.springframework.data.domain.Pageable;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static int getPage(Pageable pageable) {
        return pageable.getPageNumber();
    }

    public static int getSize(Pageable pageable) {
        return pageable.getPageSize();
    }
}
