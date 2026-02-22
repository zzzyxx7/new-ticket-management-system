package com.fuzhou.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fuzhou.common.constant.ShowCategoryConstant;
import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.common.utils.IpUtil;
import com.fuzhou.common.utils.RedisUtil;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.pojo.vo.ShowDetailVO;
import com.fuzhou.pojo.vo.ShowVO;
import com.fuzhou.pojo.vo.SessionsVO;
import com.fuzhou.server.mapper.ShowActorMapper;
import com.fuzhou.server.mapper.ShowMapper;
import com.fuzhou.server.mapper.SessionsMapper;
import com.fuzhou.server.mapper.UserInfoMapper;
import com.fuzhou.server.service.IpLocationService;
import com.fuzhou.server.service.ShowService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShowServiceImpl implements ShowService {

    private static final long HOME_SHOWS_CACHE_MINUTES = 5;

    @Autowired
    private ShowMapper showMapper;
    @Autowired
    private UserInfoMapper userMapper;
    @Autowired
    private ShowActorMapper showActorMapper;
    @Autowired
    private SessionsMapper sessionsMapper;
    @Autowired
    private IpLocationService ipLocationService;
    @Autowired
    private RedisUtil redisUtil;

//    @Override
//    public PageResult show(PageDTO homePageDTO, HttpServletRequest request) {
//        // 1. 先处理token/登录逻辑（所有前置操作）
//        Boolean isLogin = (Boolean) request.getAttribute("isLogin");
//        Long loginUserId = (Long) request.getAttribute("loginUserId");
//        if (isLogin == null) isLogin = false;
//
//        // 2. 确定查询城市（未登录=北京，已登录=用户地址）
//        String queryCity = "北京市"; // 默认北京
//        if (isLogin) {
//            String userCity = userMapper.selectCityByUserId(loginUserId);
//            if (userCity != null && !userCity.trim().isEmpty()) {
//                queryCity = userCity;
//            }
//            log.info("登录用户[{}]，查询城市：{}", loginUserId, queryCity);
//        } else {
//            log.info("未登录用户，默认查询城市：{}", queryCity);
//        }
//
//        // ========== 核心修复：PageHelper移到这里（紧挨着演出查询） ==========
//        PageHelper.startPage(homePageDTO.getPage(), homePageDTO.getPageSize());
//
//        // 3. 分页查询演出主数据（此时是PageHelper后的第一个查询，返回Page<ShowVO>）
//        List<ShowVO> showList = showMapper.selectByCity(queryCity);
//        if (CollectionUtils.isEmpty(showList)) {
//            return new PageResult(0L, List.of()); // 无数据返回空分页
//        }
//
//        // 4. 批量查询演出关联的演员
//        List<Long> showIds = showList.stream()
//                .map(ShowVO::getId)
//                .collect(Collectors.toList());
//
//        List<ShowVO.ActorVO> actorVOList = showActorMapper.selectActorsByShowIds(showIds);
//
//        // 按showId分组（需确保ActorVO有showId字段和getShowId()方法）
//        Map<Long, List<ShowVO.ActorVO>> showActorMap = actorVOList.stream()
//                .collect(Collectors.groupingBy(
//                        ShowVO.ActorVO::getShowId,
//                        Collectors.toList()
//                ));
//
//        // 绑定演员列表
//        showList.forEach(show -> {
//            List<ShowVO.ActorVO> actors = showActorMap.getOrDefault(show.getId(), List.of());
//            show.setActors(actors);
//        });
//
//        // 5. 封装分页结果（此时showList是Page类型，强转生效）
//        Page<ShowVO> page = (Page<ShowVO>) showList;
//        return new PageResult(page.getTotal(), page.getResult());
//    }

    @Override
    public Result<List<ShowVO>> getHomeShows(String city, HttpServletRequest request) {
        // 1. 确定查询城市
        String queryCity = determineCity(city, request);
        String cacheKey = redisUtil.buildKey("home", "shows", queryCity);

        // 2. 先查 Redis 缓存（参考 ticket-system）
        List<ShowVO> cachedList = redisUtil.getJson(cacheKey, new TypeReference<List<ShowVO>>() {});
        if (cachedList != null) {
            log.debug("首页推荐命中缓存: city={}", queryCity);
            return Result.success(cachedList);
        }

        // 3. 缓存未命中，查数据库
        String[] categories = ShowCategoryConstant.getHomeCategories();
        List<String> categoryList = Arrays.asList(categories);
        List<ShowVO> showList = showMapper.selectByCityAndCategories(queryCity, categoryList);

        if (CollectionUtils.isEmpty(showList)) {
            return Result.success(new ArrayList<>());
        }

        // 4. 批量查询演员信息
        enrichShowsWithActors(showList);
        // 5. 设置用户端库存信息
        setUserSideStockInfo(showList);

        // 6. 写入 Redis 缓存
        redisUtil.setJson(cacheKey, showList, HOME_SHOWS_CACHE_MINUTES, TimeUnit.MINUTES);

        return Result.success(showList);
    }

    @Override
    public Result<PageResult<ShowVO>> searchShows(String keyword, String city, Long sortId, PageDTO pageDTO) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        
        // 处理分页参数
        validatePageParams(pageDTO);
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        
        // 搜索演出（匹配演出名/明星名）
        List<ShowVO> showList = showMapper.searchByNameAndCondition(
            keyword.trim(), city, sortId
        );
        
        if (CollectionUtils.isEmpty(showList)) {
            Page<ShowVO> page = (Page<ShowVO>) showList;
            return Result.success(new PageResult<>(0L, new ArrayList<>()));
        }
        
        // 批量查询演员信息
        enrichShowsWithActors(showList);
        
        // 设置用户端库存信息
        setUserSideStockInfo(showList);
        
        // 封装分页结果
        Page<ShowVO> page = (Page<ShowVO>) showList;
        return Result.success(new PageResult<>(page.getTotal(), page.getResult()));
    }

    @Override
    public Result<PageResult<ShowVO>> getShowsByCondition(String city, Long sortId, PageDTO pageDTO) {
        // 处理分页参数
        validatePageParams(pageDTO);
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        
        // 条件查询演出
        List<ShowVO> showList = showMapper.selectByCondition(city, sortId);
        
        if (CollectionUtils.isEmpty(showList)) {
            Page<ShowVO> page = (Page<ShowVO>) showList;
            return Result.success(new PageResult<>(0L, new ArrayList<>()));
        }
        
        // 批量查询演员信息
        enrichShowsWithActors(showList);
        
        // 设置用户端库存信息
        setUserSideStockInfo(showList);
        
        // 封装分页结果
        Page<ShowVO> page = (Page<ShowVO>) showList;
        return Result.success(new PageResult<>(page.getTotal(), page.getResult()));
    }

    @Override
    public Result<ShowDetailVO> getShowDetailById(Long id) {
        if (id == null) {
            return Result.error("演出ID不能为空");
        }
        
        // 1. 查询演出基本信息
        ShowDetailVO detailVO = showMapper.selectDetailById(id);
        if (detailVO == null) {
            return Result.error("演出不存在");
        }
        
        // 2. 查询场次信息
        List<Sessions> sessionsList = sessionsMapper.selectByShowId(id);
        List<SessionsVO> sessionsVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sessionsList)) {
            for (Sessions session : sessionsList) {
                SessionsVO sessionsVO = new SessionsVO();
                sessionsVO.setId(session.getId());
                sessionsVO.setShowId(session.getShowId());
                sessionsVO.setStartTime(session.getStartTime());
                sessionsVO.setDuration(session.getDuration());
                // 用户端只返回是否有库存
                sessionsVO.setHasStock(session.getStock() != null && session.getStock() > 0);
                sessionsVOList.add(sessionsVO);
            }
        }
        detailVO.setSessions(sessionsVOList);
        
        // 3. 查询演员信息
        List<ShowVO.ActorVO> actors = showActorMapper.selectActorsByShowIds(List.of(id));
        detailVO.setActors(actors);
        
        // 4. 设置库存信息（用户端只返回是否有库存）
        boolean hasStock = sessionsList != null && sessionsList.stream()
            .anyMatch(s -> s.getStock() != null && s.getStock() > 0);
        detailVO.setHasStock(hasStock);
        
        // 5. 设置是否已开票（这里可以根据实际业务逻辑判断，暂时简单处理）
        // 如果有场次且场次开始时间未到，认为已开票
        boolean issued = !CollectionUtils.isEmpty(sessionsList) && 
            sessionsList.stream().anyMatch(s -> s.getStartTime() != null);
        detailVO.setIssued(issued);
        
        return Result.success(detailVO);
    }

    /**
     * 确定查询城市（优先级：手动指定 > IP定位 > 用户地址 > 默认北京）
     * 已登录用户：优先根据IP定位返回对应地区演出
     */
    private String determineCity(String city, HttpServletRequest request) {
        // 1. 如果传入了城市参数，优先使用
        if (city != null && !city.trim().isEmpty()) {
            log.info("使用手动指定的城市：{}", city);
            return city.trim();
        }
        
        // 2. 获取客户端IP并定位城市（已登录用户优先使用IP定位）
        String clientIp = IpUtil.getClientIp(request);
        log.info("客户端IP：{}", clientIp);
        
        String ipCity = ipLocationService.getCityByIp(clientIp);
        if (ipCity != null && !ipCity.trim().isEmpty()) {
            log.info("IP定位成功，城市：{}", ipCity);
            return ipCity;
        }
        
        // 3. IP定位失败，检查是否登录，使用用户地址
        Boolean isLogin = (Boolean) request.getAttribute("isLogin");
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        if (isLogin == null) isLogin = false;
        
        if (isLogin && loginUserId != null) {
            String userCity = userMapper.selectCityByUserId(loginUserId);
            if (userCity != null && !userCity.trim().isEmpty()) {
                log.info("登录用户[{}]，使用用户地址城市：{}", loginUserId, userCity);
                return userCity;
            }
        }
        
        // 4. 默认返回北京
        log.info("IP定位失败且未设置用户地址，默认查询城市：北京市");
        return "北京市";
    }

    /**
     * 批量查询并绑定演员信息
     */
    private void enrichShowsWithActors(List<ShowVO> showList) {
        if (CollectionUtils.isEmpty(showList)) {
            return;
        }
        
        List<Long> showIds = showList.stream()
            .map(ShowVO::getId)
            .collect(Collectors.toList());
        
        List<ShowVO.ActorVO> actorVOList = showActorMapper.selectActorsByShowIds(showIds);
        
        Map<Long, List<ShowVO.ActorVO>> showActorMap = actorVOList.stream()
            .collect(Collectors.groupingBy(ShowVO.ActorVO::getShowId, Collectors.toList()));
        
        showList.forEach(show -> {
            List<ShowVO.ActorVO> actors = showActorMap.getOrDefault(show.getId(), List.of());
            show.setActors(actors);
        });
    }

    /**
     * 设置用户端库存信息（只返回是否有库存，不返回具体数量）
     */
    private void setUserSideStockInfo(List<ShowVO> showList) {
        if (CollectionUtils.isEmpty(showList)) {
            return;
        }
        
        // 批量查询库存信息
        List<Long> showIds = showList.stream()
            .map(ShowVO::getId)
            .collect(Collectors.toList());
        
        // 查询每个演出的总库存（所有场次库存之和）
        List<Map<String, Object>> stockList = showMapper.selectStockByShowIds(showIds);
        
        // 转换为 Map，方便查找
        Map<Long, Integer> stockMap = stockList.stream()
            .collect(Collectors.toMap(
                item -> Long.valueOf(item.get("showId").toString()),
                item -> ((Number) item.get("totalStock")).intValue()
            ));
        
        // 设置是否有库存
        showList.forEach(show -> {
            Integer totalStock = stockMap.getOrDefault(show.getId(), 0);
            show.setHasStock(totalStock > 0);
        });
    }

    /**
     * 验证分页参数
     */
    private void validatePageParams(PageDTO pageDTO) {
        if (pageDTO.getPage() == null || pageDTO.getPage() < 1) {
            pageDTO.setPage(1);
        }
        if (pageDTO.getPageSize() == null || pageDTO.getPageSize() < 1) {
            pageDTO.setPageSize(10);
        }
        if (pageDTO.getPageSize() > 100) {
            pageDTO.setPageSize(100);
        }
    }
}

