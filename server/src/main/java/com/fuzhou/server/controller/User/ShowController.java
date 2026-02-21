package com.fuzhou.server.controller.User;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.CreateOrderDTO;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.ShowDetailVO;
import com.fuzhou.pojo.vo.ShowVO;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.server.mq.TicketOrderSender;
import com.fuzhou.server.service.SessionsService;
import com.fuzhou.server.service.ShowService;
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
    private TicketOrderSender ticketOrderSender;

    @Autowired
    private SessionsService sessionsService;


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
     * 用户抢票 / 创建订单（异步版）
     * 路径：POST /user/show/buy
     * 前端拿到“已进入排队”后，可以通过“我的订单列表 / 订单详情”接口轮询查看结果
     */
    @PostMapping("/buy")
    public Result<String> createOrder(@RequestBody CreateOrderDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户[{}]发起抢票（异步），请求参数：{}", userId, dto);

        if (dto.getSessionId() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            return Result.error("参数错误：场次ID和数量不能为空且必须大于0");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 先查库存，不足则直接返回错误，避免接口只返回“等待”而用户不知道失败
        Sessions session = sessionsService.getById(dto.getSessionId());
        if (session == null) {
            return Result.error("场次不存在");
        }
        Integer stock = session.getStock();
        if (stock == null || stock < dto.getQuantity()) {
            return Result.error("库存不足，当前剩余 " + (stock == null ? 0 : stock) + " 张");
        }

        ticketOrderSender.send(dto.getSessionId(), dto.getQuantity(), userId);
        return Result.success("已进入排队，请稍后在“我的订单”中查看是否抢票成功");
    }
}
