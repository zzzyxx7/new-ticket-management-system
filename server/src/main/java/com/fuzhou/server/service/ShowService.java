package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.ShowDetailVO;
import com.fuzhou.pojo.vo.ShowVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
public interface ShowService {
//    PageResult show(PageDTO homePageDTO, HttpServletRequest request);

    /**
     * 首页获取地区与分类演出列表
     * 默认返回北京地区四大类演出
     * 已登录：根据用户IP返回对应地区演出
     */
    Result<List<ShowVO>> getHomeShows(String city, HttpServletRequest request);

    //  搜索演出（匹配演出名/明星名）
    Result<PageResult<ShowVO>> searchShows(String keyword, String city, Long sortId, PageDTO pageDTO);

     // 条件查询演出（城市、分类、分页）
    Result<PageResult<ShowVO>> getShowsByCondition(String city, Long sortId, PageDTO pageDTO);

     // 获取演出详情
     // 包含：场次信息、票档信息、库存信息、是否已开票
    Result<ShowDetailVO> getShowDetailById(Long id);
}
