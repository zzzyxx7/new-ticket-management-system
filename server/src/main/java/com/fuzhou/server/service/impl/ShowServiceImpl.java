package com.fuzhou.server.service.impl;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.PageDTO;
import com.fuzhou.pojo.vo.ShowVO;
import com.fuzhou.server.mapper.ShowActorMapper;
import com.fuzhou.server.mapper.ShowMapper;
import com.fuzhou.server.mapper.UserInfoMapper;
import com.fuzhou.server.service.ShowService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShowServiceImpl implements ShowService {
    @Autowired
    private ShowMapper showMapper;
    @Autowired
    private UserInfoMapper userMapper;
    @Autowired
    private ShowActorMapper showActorMapper;

    @Override
    public PageResult show(PageDTO homePageDTO, HttpServletRequest request) {
        // 1. 先处理token/登录逻辑（所有前置操作）
        Boolean isLogin = (Boolean) request.getAttribute("isLogin");
        Long loginUserId = (Long) request.getAttribute("loginUserId");
        if (isLogin == null) isLogin = false;

        // 2. 确定查询城市（未登录=北京，已登录=用户地址）
        String queryCity = "北京市"; // 默认北京
        if (isLogin) {
            String userCity = userMapper.selectCityByUserId(loginUserId);
            if (userCity != null && !userCity.trim().isEmpty()) {
                queryCity = userCity;
            }
            log.info("登录用户[{}]，查询城市：{}", loginUserId, queryCity);
        } else {
            log.info("未登录用户，默认查询城市：{}", queryCity);
        }

        // ========== 核心修复：PageHelper移到这里（紧挨着演出查询） ==========
        PageHelper.startPage(homePageDTO.getPage(), homePageDTO.getPageSize());

        // 3. 分页查询演出主数据（此时是PageHelper后的第一个查询，返回Page<ShowVO>）
        List<ShowVO> showList = showMapper.selectByCity(queryCity);
        if (CollectionUtils.isEmpty(showList)) {
            return new PageResult(0L, List.of()); // 无数据返回空分页
        }

        // 4. 批量查询演出关联的演员
        List<Long> showIds = showList.stream()
                .map(ShowVO::getId)
                .collect(Collectors.toList());

        List<ShowVO.ActorVO> actorVOList = showActorMapper.selectActorsByShowIds(showIds);

        // 按showId分组（需确保ActorVO有showId字段和getShowId()方法）
        Map<Long, List<ShowVO.ActorVO>> showActorMap = actorVOList.stream()
                .collect(Collectors.groupingBy(
                        ShowVO.ActorVO::getShowId,
                        Collectors.toList()
                ));

        // 绑定演员列表
        showList.forEach(show -> {
            List<ShowVO.ActorVO> actors = showActorMap.getOrDefault(show.getId(), List.of());
            show.setActors(actors);
        });

        // 5. 封装分页结果（此时showList是Page类型，强转生效）
        Page<ShowVO> page = (Page<ShowVO>) showList;
        return new PageResult(page.getTotal(), page.getResult());

    }

}

