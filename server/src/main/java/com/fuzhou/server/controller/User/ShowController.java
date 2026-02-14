package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.CreateOrderDTO;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.ShowDetailVO;
import com.fuzhou.pojo.vo.ShowVO;
import com.fuzhou.server.service.ShowService;
import com.fuzhou.server.service.UserOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/show")
@Slf4j
public class ShowController {
    @Autowired
    private ShowService showService;

    @Autowired
    private UserOrderService userOrderService;


//    @GetMapping
//    public Result show(PageDTO homePageDTO, HttpServletRequest request){
//        PageResult pageResult = showService.show(homePageDTO, request);
//        return Result.success(pageResult);
//    }

    /**
     * 首页获取地区与分类演出列表
     * 默认返回北京地区四大类演出
     * 已登录：根据用户IP返回对应地区演出
     */
    @GetMapping
    public Result<List<ShowVO>> getHomeShows(
            @RequestParam(required = false) String city,
            HttpServletRequest request) {
        return showService.getHomeShows(city, request);
    }

    /**
     * 搜索演出（匹配演出名/明星名）
     */
    @GetMapping("/search")
    public Result<PageResult<ShowVO>> searchShows(
            @RequestParam String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long sortId,
            PageDTO pageDTO) {
        return showService.searchShows(keyword, city, sortId, pageDTO);
    }

    /**
     * 条件查询演出（城市、分类、分页）
     */
    @GetMapping("/condition")
    public Result<PageResult<ShowVO>> getShowsByCondition(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long sortId,
            PageDTO pageDTO) {
        return showService.getShowsByCondition(city, sortId, pageDTO);
    }

    /**
     * 获取演出详情
     * 包含：场次信息、票档信息、库存信息、是否已开票
     * 请求示例：GET /user/show/detail?id=1
     */
    @GetMapping("/detail")
    public Result<ShowDetailVO> getShowDetailById(@RequestParam Long id) {
        return showService.getShowDetailById(id);
    }

    /**
     * 用户抢票 / 创建订单
     * 路径：POST /user/show/order
     */
    @PostMapping("/buy")
    public Result<String> createOrder(@RequestBody CreateOrderDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户[{}]发起抢票，请求参数：{}", userId, dto);
        return userOrderService.createOrder(dto.getSessionId(), dto.getQuantity(), userId);
    }
}
