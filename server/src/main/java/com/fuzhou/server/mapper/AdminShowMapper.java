package com.fuzhou.server.mapper;

import com.fuzhou.pojo.dto.ShowPageQueryDTO;
import com.fuzhou.pojo.entity.Show;
import com.fuzhou.pojo.vo.AdminShowVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 管理端演出Mapper接口
 */
@Mapper
public interface AdminShowMapper {
    /**
     * 分页查询演出列表（支持多条件筛选）
     * @param dto 查询条件（包含分页参数和筛选条件）
     * @return 演出列表
     */
    List<AdminShowVO> pageQuery(ShowPageQueryDTO dto);

    /**
     * 根据ID查询演出详情（包含分类名称和库存数量）
     * @param id 演出ID
     * @return 演出详情
     */
    AdminShowVO getById(Long id);

    /**
     * 新增演出
     * @param show 演出信息
     */
    void insert(Show show);

    /**
     * 修改演出信息（动态更新，只更新传入的字段）
     * @param show 演出信息
     */
    void update(Show show);

    /**
     * 删除演出（根据ID）
     * @param id 演出ID
     */
    void deleteById(Long id);
}
