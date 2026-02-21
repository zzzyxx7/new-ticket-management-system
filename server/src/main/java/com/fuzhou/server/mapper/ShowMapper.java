package com.fuzhou.server.mapper;

import com.fuzhou.pojo.vo.ShowDetailVO;
import com.fuzhou.pojo.vo.ShowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShowMapper {

    //  根据城市查询演出列表

    List<ShowVO> selectByCity(String queryCity);


    //  根据城市和分类列表查询演出（首页推荐）

    List<ShowVO> selectByCityAndCategories(@Param("city") String city, 
                                            @Param("categories") List<String> categories);


     // 搜索演出（匹配演出名/明星名）

    List<ShowVO> searchByNameAndCondition(@Param("keyword") String keyword,
                                         @Param("city") String city,
                                         @Param("sortId") Long sortId);


    //  条件查询演出（城市、分类）

    List<ShowVO> selectByCondition(@Param("city") String city,
                                    @Param("sortId") Long sortId);


     // 根据ID查询演出详情

    ShowDetailVO selectDetailById(Long id);

    /** 根据演出ID查询演出名称（用于订单标题等） */
    String getTitleById(@Param("id") Long id);


     // 批量查询演出库存（所有场次库存之和）
     // 返回 List<Map>，每个 Map 包含 showId 和 totalStock

    List<Map<String, Object>> selectStockByShowIds(@Param("showIds") List<Long> showIds);
}
