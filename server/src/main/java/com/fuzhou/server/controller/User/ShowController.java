package com.fuzhou.server.controller.User;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.server.service.ShowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/show")
public class ShowController {
    @Autowired
    private ShowService showService;

    /**
     * 首页展示演出
     */
    @GetMapping
    public Result show(PageDTO homePageDTO, HttpServletRequest request){
        PageResult pageResult = showService.show(homePageDTO, request);
        return Result.success(pageResult);
    }
}
