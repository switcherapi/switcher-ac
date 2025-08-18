package com.switcherapi.ac.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

@UtilityClass
public class FileUtil {

    @SneakyThrows
    public static String getFilePathFromResource(String arg) {
        if (StringUtils.isNotBlank(arg)) {
            return ResourceUtils.getFile(arg).getAbsoluteFile().getPath();
        }
        return StringUtils.EMPTY;
    }
}
