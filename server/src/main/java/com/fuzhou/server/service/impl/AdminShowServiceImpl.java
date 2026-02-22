package com.fuzhou.server.service.impl;

import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.utils.RedisUtil;
import com.fuzhou.pojo.dto.ShowPageQueryDTO;
import com.fuzhou.pojo.entity.Show;
import com.fuzhou.pojo.vo.AdminShowVO;
import com.fuzhou.server.mapper.AdminShowMapper;
import com.fuzhou.server.service.AdminShowService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
public class AdminShowServiceImpl implements AdminShowService {

    private static final String HOME_SHOWS_CACHE_PATTERN = "home:shows:*";

    @Autowired
    private AdminShowMapper adminShowMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public PageResult showPageQuery(ShowPageQueryDTO dto) {
        // 处理分页参数
        if (dto.getPage() == null || dto.getPage() < 1) {
            dto.setPage(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }

        // 使用PageHelper分页
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // 查询演出列表
        List<AdminShowVO> list = adminShowMapper.pageQuery(dto);

        // 封装分页结果
        Page<AdminShowVO> page = (Page<AdminShowVO>) list;
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public AdminShowVO getShowById(Long id) {
        if (id == null) {
            throw new BaseException("演出ID不能为空");
        }
        AdminShowVO showVO = adminShowMapper.getById(id);
        if (showVO == null) {
            throw new BaseException("演出不存在");
        }
        return showVO;
    }

    @Override
    @Transactional
    public void addShow(Show show) {
        // 参数校验
        if (show == null) {
            throw new BaseException("演出信息不能为空");
        }
        if (!StringUtils.hasText(show.getTitle())) {
            throw new BaseException("演出名称不能为空");
        }
        if (!StringUtils.hasText(show.getVenueName())) {
            throw new BaseException("场馆名称不能为空");
        }

        // 如果没传入完整地址，则自动拼接
        if (!StringUtils.hasText(show.getFullAddress())) {
            show.setFullAddress(buildFullAddress(
                show.getProvince(),
                show.getCity(),
                show.getDistrict(),
                show.getVenueName(),
                show.getDetailAddress()
            ));
        }

        // 新增演出
        adminShowMapper.insert(show);
        redisUtil.deleteByPattern(HOME_SHOWS_CACHE_PATTERN);
        log.info("管理员新增演出，演出ID：{}，演出名称：{}", show.getId(), show.getTitle());
    }

    @Override
    @Transactional
    public void updateShow(Show show) {
        if (show == null || show.getId() == null) {
            throw new BaseException("演出信息或演出ID不能为空");
        }

        // 先查询演出是否存在
        AdminShowVO existingShow = adminShowMapper.getById(show.getId());
        if (existingShow == null) {
            throw new BaseException("演出不存在");
        }

        // 如果修改了地址相关字段，且没有传入完整地址，则自动拼接
        boolean addressChanged = StringUtils.hasText(show.getProvince()) || 
                                 StringUtils.hasText(show.getCity()) || 
                                 StringUtils.hasText(show.getDistrict()) || 
                                 StringUtils.hasText(show.getVenueName()) || 
                                 StringUtils.hasText(show.getDetailAddress());
        
        if (addressChanged && !StringUtils.hasText(show.getFullAddress())) {
            // 拼接完整地址：优先用新值，没有就用旧值
            String province = StringUtils.hasText(show.getProvince()) ? show.getProvince() : existingShow.getProvince();
            String city = StringUtils.hasText(show.getCity()) ? show.getCity() : existingShow.getCity();
            String district = StringUtils.hasText(show.getDistrict()) ? show.getDistrict() : existingShow.getDistrict();
            String venueName = StringUtils.hasText(show.getVenueName()) ? show.getVenueName() : existingShow.getVenueName();
            String detailAddress = StringUtils.hasText(show.getDetailAddress()) ? show.getDetailAddress() : existingShow.getDetailAddress();
            
            show.setFullAddress(buildFullAddress(province, city, district, venueName, detailAddress));
        }

        // 更新演出
        adminShowMapper.update(show);
        redisUtil.deleteByPattern(HOME_SHOWS_CACHE_PATTERN);
        log.info("管理员修改演出，演出ID：{}", show.getId());
    }

    @Override
    @Transactional
    public void deleteShowById(Long id) {
        if (id == null) {
            throw new BaseException("演出ID不能为空");
        }

        // 先查询演出是否存在
        AdminShowVO existingShow = adminShowMapper.getById(id);
        if (existingShow == null) {
            throw new BaseException("演出不存在");
        }

        // 检查是否有关联的场次（如果有场次，删除可能会失败，但让数据库外键约束来处理）
        // 注意：如果该演出有关联的场次（sessions），删除会失败（外键约束）
        // 实际项目中可能需要先删除关联数据，或者使用软删除

        // 删除演出
        adminShowMapper.deleteById(id);
        redisUtil.deleteByPattern(HOME_SHOWS_CACHE_PATTERN);
        log.info("管理员删除演出，演出ID：{}，演出名称：{}", id, existingShow.getTitle());
    }

    /**
     * 拼接完整地址
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param venueName 场馆名称
     * @param detailAddress 详细地址
     * @return 完整地址
     */
    private String buildFullAddress(String province, String city, String district, 
                                     String venueName, String detailAddress) {
        StringBuilder fullAddr = new StringBuilder();
        if (StringUtils.hasText(province)) {
            fullAddr.append(province);
        }
        if (StringUtils.hasText(city)) {
            fullAddr.append(city);
        }
        if (StringUtils.hasText(district)) {
            fullAddr.append(district);
        }
        if (StringUtils.hasText(venueName)) {
            fullAddr.append(venueName);
        }
        if (StringUtils.hasText(detailAddress)) {
            fullAddr.append(detailAddress);
        }
        return fullAddr.toString();
    }
}

