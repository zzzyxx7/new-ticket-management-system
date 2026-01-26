package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.PageDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface ShowService {
    PageResult show(PageDTO homePageDTO, HttpServletRequest request);
}
