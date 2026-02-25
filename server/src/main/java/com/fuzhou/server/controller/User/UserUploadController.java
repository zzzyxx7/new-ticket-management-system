package com.fuzhou.server.controller.User;

import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户端图片上传（头像、演出封面等都可以用这个接口先上传拿 URL）
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserUploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 上传图片到 OSS，返回图片 URL
     *
     * POST /user/upload
     * Header: token: <access_token>
     * Body: form-data, key=file, value=选择的图片
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("上传文件不能为空");
            }
            String originalFilename = file.getOriginalFilename();
            String url = aliOssUtil.upload(file.getBytes(), originalFilename);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error("上传文件失败");
        }
    }
}

