package com.fuzhou.server.controller.Admin;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.PageResult;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.dto.ShowPageQueryDTO;
import com.fuzhou.pojo.entity.Show;
import com.fuzhou.pojo.vo.AdminShowVO;
import com.fuzhou.server.service.AdminShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

 //管理端演出管理Controller

@RestController
@RequestMapping("/admin/show")
@Slf4j
public class AdminShowController {
    @Autowired
    private AdminShowService adminShowService;


     //分页查询演出列表

    @GetMapping
    public Result<PageResult> pageQuery(ShowPageQueryDTO dto) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]分页查询演出列表，条件：{}", adminId, dto);
        PageResult pageResult = adminShowService.showPageQuery(dto);
        return Result.success(pageResult);
    }

    //查询演出详情（包含库存数量）
    // GET /admin/show/detail?id=1

    @GetMapping("/detail")
    public Result<AdminShowVO> getById(Long id) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]查询演出详情，演出ID：{}", adminId, id);
        AdminShowVO showVO = adminShowService.getShowById(id);
        return Result.success(showVO);
    }


     // 新增演出
      //POST /admin/show
      //Body: { "title": "演出名称", "sortId": 1, ... }
    @PostMapping
    public Result add(@RequestBody Show show) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]新增演出，演出信息：{}", adminId, show);
        adminShowService.addShow(show);
        return Result.success("新增成功");
    }

//       修改演出信息
//     PUT /admin/show
//    Body: { "id": 1, "title": "新名称", ... }
//
    @PutMapping
    public Result update(@RequestBody Show show) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]修改演出信息，演出信息：{}", adminId, show);
        adminShowService.updateShow(show);
        return Result.success("修改成功");
    }


     // 删除演出
     // DELETE /admin/show?id=1
    @DeleteMapping
    public Result deleteById(Long id) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]删除演出，演出ID：{}", adminId, id);
        adminShowService.deleteShowById(id);
        return Result.success("删除成功");
    }
}

