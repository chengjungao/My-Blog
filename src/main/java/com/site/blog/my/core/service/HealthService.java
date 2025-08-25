package com.site.blog.my.core.service;

import org.springframework.web.multipart.MultipartFile;

public interface HealthService {

    /**
     * 分析图片中的成分
     * @param image 图片文件
     * @return 分析结果
     */
    String analyzeIngredients(MultipartFile image);

    /**
     * 估算图片中的食物热量
     * @param image 图片文件
     * @return 热量估算结果
     */
    String estimateCalories(MultipartFile image);
}
