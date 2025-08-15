package com.codingrecipe.board.util;

import java.nio.file.Paths;

public class FileUtil {

    /**
     * 파일 확장자 추출
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }

    /**
     * 파일 키에서 기본 이름(파일 이름) 추출
     */
    public static String getBaseName(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) return "";
        return Paths.get(fileKey).getFileName().toString();
    }

}
