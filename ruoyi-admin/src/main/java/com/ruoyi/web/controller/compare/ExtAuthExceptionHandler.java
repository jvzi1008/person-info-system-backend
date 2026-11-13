package com.ruoyi.web.controller.compare;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.compare.exception.ExtAuthException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExtAuthExceptionHandler {

    @ExceptionHandler(ExtAuthException.class)
    public ResponseEntity<AjaxResult> handleExtAuthException(ExtAuthException e) {
        return ResponseEntity.status(e.getCode())
                .body(AjaxResult.error(e.getCode(), e.getMessage()));
    }
}
