package ru.javaops.startup.app.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.javaops.startup.common.error.ErrorType;

import java.util.List;
import java.util.Map;

import static ru.javaops.startup.common.error.ErrorType.BAD_REQUEST;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class UIExceptionHandler extends BasicExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ModelAndView bindException(BindException ex, HttpServletRequest request) {
        List<String> errorList = errorMessageHandler.getErrorList(ex.getBindingResult());
        String path = request.getRequestURI();
        log.warn(ERR_PFX + "BindException {} at request {}", errorList, path);
        return getExceptionView(ex, path, BAD_REQUEST, String.join("<br>", errorList));
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView exception(Exception ex, HttpServletRequest request) {
        return processException(ex, request, UIExceptionHandler::getExceptionView);
    }

    // Process error from ErrorController
    ModelAndView processError(@Nullable Throwable th, String path, @Nullable Integer intStatus, String msg) {
        return super.processError(th, path, msg, UIExceptionHandler::getExceptionView, () -> {
            HttpStatus status = getStatus(intStatus);
            return getView(th, status, ErrorType.of(status).title, msg);
        });
    }

    private static ModelAndView getExceptionView(@Nullable Throwable ex, String path, ErrorType type, String msg) {
        return getView(ex, type.status, type.title, msg);
    }

    private static ModelAndView getView(@Nullable Throwable ex, HttpStatus status, String title, String msg) {
        ModelAndView modelAndView = ex instanceof NoResourceFoundException ?
                new ModelAndView("404") :
                new ModelAndView("exception",
                        Map.of("title", title, "status", status, "msg", msg));
        modelAndView.setStatus(status);
        return modelAndView;
    }
}