package com.fuzhou.server.handler;


import com.fuzhou.common.exception.BaseException;
import com.fuzhou.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;


/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<?> exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理请求体 JSON 格式错误
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.error("JSON 解析错误：{}", ex.getMessage());
        return Result.error("请求体 JSON 格式错误，请检查是否为合法的 JSON 格式（如是否少了引号或逗号）");
    }

    /**
     * 处理 form-data 上传缺少 file 等 part
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<?> handleMissingServletRequestPart(MissingServletRequestPartException ex) {
        log.error("缺少请求 part：{}", ex.getMessage());
        return Result.error("缺少表单字段：" + ex.getRequestPartName());
    }

    /**
     * 处理缺少 query/form 参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.error("缺少请求参数：{}", ex.getMessage());
        return Result.error("缺少请求参数：" + ex.getParameterName());
    }

    /**
     * 处理参数类型不匹配（例如 pageSize 传了字符串）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("参数类型不匹配：{}", ex.getMessage());
        Class<?> required = ex.getRequiredType();
        String requiredType = required == null ? "未知类型" : required.getSimpleName();
        return Result.error("参数类型错误：" + ex.getName() + "，期望类型：" + requiredType);
    }

    /**
     * 处理 @Valid 校验失败（JSON body）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + "：" + err.getDefaultMessage())
                .orElse("参数校验失败");
        log.error("参数校验失败：{}", msg);
        return Result.error(msg);
    }

    /**
     * 处理表单/Query 参数绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + "：" + err.getDefaultMessage())
                .orElse("参数校验失败");
        log.error("参数绑定失败：{}", msg);
        return Result.error(msg);
    }

    /**
     * 处理数据库唯一约束冲突（如账号/邮箱重复）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKey(DuplicateKeyException ex) {
        String raw = ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage();
        log.error("唯一约束冲突：{}", raw);
        if (raw != null) {
            if (raw.contains("uk_account") || raw.contains("account")) {
                return Result.error("账号已存在，请更换账号");
            }
            if (raw.contains("uk_email") || raw.contains("email")) {
                return Result.error("该邮箱已被其他账号使用");
            }
        }
        return Result.error("数据已存在，无法重复提交");
    }

    /**
     * 处理不支持的请求方法
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.error("请求方法不支持：{}", ex.getMessage());
        return Result.error("请求方法不支持：" + ex.getMethod());
    }

    /**
     * 兜底：未被捕获的异常，避免返回空响应/不友好错误
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOtherException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统异常，请稍后再试");
    }

}
