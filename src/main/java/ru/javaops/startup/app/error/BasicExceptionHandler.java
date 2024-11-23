package ru.javaops.startup.app.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.javaops.startup.common.error.AppException;
import ru.javaops.startup.common.error.ErrorType;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static ru.javaops.startup.common.error.ErrorType.*;

@Slf4j
public class BasicExceptionHandler {
    public static final String ERR_PFX = "ERR# ";

    @Autowired
    protected ErrorMessageHandler errorMessageHandler;

    //    https://stackoverflow.com/a/52254601/548473
    static final Map<Class<? extends Throwable>, ErrorType> HTTP_STATUS_MAP = new LinkedHashMap<>() {
        {
// more specific first
            put(NoResourceFoundException.class, NOT_FOUND);
            put(AuthenticationException.class, UNAUTHORIZED);
            put(FileNotFoundException.class, NOT_FOUND);
            put(NoHandlerFoundException.class, NOT_FOUND);
            put(UnsupportedOperationException.class, APP_ERROR);
            put(EntityNotFoundException.class, DATA_CONFLICT);
            put(DataIntegrityViolationException.class, DATA_CONFLICT);
            put(IllegalArgumentException.class, BAD_DATA);
            put(ValidationException.class, BAD_REQUEST);
            put(HttpRequestMethodNotSupportedException.class, BAD_REQUEST);
            put(ServletRequestBindingException.class, BAD_REQUEST);
            put(RequestRejectedException.class, BAD_REQUEST);
            put(AccessDeniedException.class, FORBIDDEN);
        }
    };

    public static HttpStatus getStatus(Integer status) {
        return status == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(status);
    }

    interface Processor<T> {
        T process(Throwable ex, String path, ErrorType type, String msg);
    }

    protected <T> T processException(@NonNull Throwable ex, HttpServletRequest request, Processor<T> processor) {
        return processException(ex, request.getRequestURI(), processor);
    }

    protected <T> T processException(@NonNull Throwable ex, String path, Processor<T> processor) {
        Optional<ErrorType> optType = findErrorType(ex);
        if (optType.isEmpty()) {
            Throwable root = getRootCause(ex);
            if (root != ex) {
                optType = findErrorType(root);
                ex = root;
            }
        }
        if (optType.isPresent()) {
            log.error(ERR_PFX + "Exception {} at request {}", ex, path);
            return processor.process(ex, path, optType.get(), ex.getLocalizedMessage());
        } else {
            log.error(ERR_PFX + "Exception " + ex + " at request " + path, ex);
            return processor.process(ex, path, APP_ERROR, ex.getClass().getSimpleName());
        }
    }

    // Process error from ErrorController
    public <T> T processError(@Nullable Throwable th, String path, String msg, Processor<T> processor, Supplier<T> supplier) {
        if (th != null) {
            return processException(th, path, processor);
        }
        log.error(ERR_PFX + "Exception " + msg + " at request " + path);
        return supplier.get();
    }

    private Optional<ErrorType> findErrorType(Throwable ex) {
        if (ex instanceof AppException ae) {
            return Optional.of(ae.getErrorType());
        }
        Class<? extends Throwable> exClass = ex.getClass();
        return HTTP_STATUS_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(exClass))
                .findAny().map(Map.Entry::getValue);
    }

    //  https://stackoverflow.com/a/65442410/548473
    @NonNull
    public static Throwable getRootCause(@NonNull Throwable t) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }
}
