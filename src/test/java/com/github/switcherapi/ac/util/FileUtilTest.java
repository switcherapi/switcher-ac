package com.github.switcherapi.ac.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileUtilTest {

    @Test
    void shouldReturnFilePathFromResource() {
        var path = FileUtil.getFilePathFromResource("classpath:application.properties");
        assertNotEquals(StringUtils.EMPTY, path);
    }

    @Test
    void shouldNotReturnFilePathFromResource() {
        var path = FileUtil.getFilePathFromResource("");
        assertEquals(StringUtils.EMPTY, path);
    }
}
