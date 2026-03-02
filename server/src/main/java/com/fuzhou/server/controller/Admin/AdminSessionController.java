package com.fuzhou.server.controller.Admin;

import com.fuzhou.common.context.BaseContext;
import com.fuzhou.common.result.Result;
import com.fuzhou.pojo.entity.Sessions;
import com.fuzhou.server.service.SessionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端场次管理：增删改查、单独维护库存
 */
@RestController
@RequestMapping("/admin/session")
@Slf4j
public class AdminSessionController {

    @Autowired
    private SessionsService sessionsService;

    /**
     * 根据演出ID查询该演出的所有场次
     * GET /admin/session/list?showId=1
     */
    @GetMapping("/list")
    public Result<List<Sessions>> listByShowId(@RequestParam Long showId) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]查询演出场次列表，showId={}", adminId, showId);
        return Result.success(sessionsService.listByShowId(showId));
    }

    /**
     * 新增场次
     * POST /admin/session
     */
    @PostMapping
    public Result<String> create(@RequestBody Sessions sessions) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]新增场次，请求参数：{}", adminId, sessions);
        sessionsService.createSession(sessions);
        return Result.success("新增场次成功");
    }

    /**
     * 修改场次（时间、价格、库存等）
     * PUT /admin/session
     */
    @PutMapping
    public Result<String> update(@RequestBody Sessions sessions) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]修改场次，请求参数：{}", adminId, sessions);
        sessionsService.updateSession(sessions);
        return Result.success("修改场次成功");
    }

    /**
     * 删除场次
     * DELETE /admin/session?id=1
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam Long id) {
        Long adminId = BaseContext.getCurrentId();
        log.info("管理员[{}]删除场次，sessionId={}", adminId, id);
        sessionsService.deleteSession(id);
        return Result.success("删除场次成功");
    }
}

