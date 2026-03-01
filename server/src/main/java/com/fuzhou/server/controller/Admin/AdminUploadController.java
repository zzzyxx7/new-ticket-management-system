package com.fuzhou.server.controller.Admin;

import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 管理端图片上传 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminUploadController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    @PostMapping("/upload/show-cover")
    public Result<String> uploadShowCover(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    @PostMapping("/upload/order-cover")
    public Result<String> uploadOrderCover(@RequestParam("file") MultipartFile file) {
        return doUpload(file);
    }

    private Result<String> doUpload(MultipartFile file) {
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

