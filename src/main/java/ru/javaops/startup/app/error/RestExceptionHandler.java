package ru.javaops.startup.app.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.javaops.startup.common.error.ErrorType;

import java.net.URI;
import java.util.Map;

import static ru.javaops.startup.common.error.ErrorType.BAD_REQUEST;

@RestControllerAdvice(annotations = RestController.class)
@AllArgsConstructor
@Slf4j
public class RestExceptionHandler extends BasicExceptionHandler {

    @ExceptionHandler(BindException.class)
    ProblemDetail bindException(BindException ex, HttpServletRequest request) {
        Map<String, String> invalidParams = errorMessageHandler.getErrorMap(ex.getBindingResult());
        String path = request.getRequestURI();
        log.warn(ERR_PFX + "BindException with invalidParams {} at request {}", invalidParams, path);
        return createParamsProblemDetail(ex, path, BAD_REQUEST, "BindException", Map.of("invalid_params", invalidParams));
    }

    //   https://howtodoinjava.com/spring-mvc/spring-problemdetail-errorresponse/#5-adding-problemdetail-to-custom-exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail exception(Throwable ex, HttpServletRequest request) {
        return processException(ex, request, this::createProblemDetail);
    }

    // Process error from ErrorController
    ProblemDetail processError(@Nullable Throwable th, String path, @Nullable Integer intStatus, String msg) {
        return super.processError(th, path, msg, this::createProblemDetail, () -> {
            HttpStatus status = BasicExceptionHandler.getStatus(intStatus);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, msg);
            pd.setTitle(ErrorType.of(status).title);
            pd.setInstance(URI.create(path));
            return pd;
        });
    }

    //    https://datatracker.ietf.org/doc/html/rfc7807
    private ProblemDetail createProblemDetail(Throwable ex, String path, ErrorType type, String defaultDetail) {
        return createParamsProblemDetail(ex, path, type, defaultDetail, Map.of());
    }

    private ProblemDetail createParamsProblemDetail(Throwable ex, String path, ErrorType type, String defaultDetail, @NonNull Map<String, Object> additionalParams) {
        ErrorResponse.Builder builder = ErrorResponse.builder(ex, type.status, defaultDetail);
        ProblemDetail pd = builder
                .title(type.title).instance(URI.create(path))
                .build().updateAndGetBody(errorMessageHandler.getMessageSource(), LocaleContextHolder.getLocale());
        additionalParams.forEach(pd::setProperty);
        return pd;
    }
}
