package com.fuzhou.server.service;

import com.fuzhou.common.result.PageResult;
import com.fuzhou.pojo.dto.ShowPageQueryDTO;
import com.fuzhou.pojo.entity.Show;
import com.fuzhou.pojo.vo.AdminShowVO;

/**
 * 管理端演出Service接口
 */
public interface AdminShowService {

     // 分页查询演出列表（支持多条件筛选）

    PageResult showPageQuery(ShowPageQueryDTO dto);


     // 根据演出ID查询演出详情（包含库存数量）

    AdminShowVO getShowById(Long id);


     // 新增演出
     //@param show 演出信息

    void addShow(Show show);


     // 修改演出信息
     //@param show 演出信息

    void updateShow(Show show);


     // 删除演出
     //@param id 演出ID

    void deleteShowById(Long id);
}

