package ru.javaops.startup.app.error;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import ru.javaops.startup.app.config.SecurityConfig;

import java.util.Map;

import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.*;

@Controller
@RequiredArgsConstructor
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    public static final ErrorAttributeOptions ATTRIBUTE_OPTIONS = ErrorAttributeOptions.of(MESSAGE, EXCEPTION, BINDING_ERRORS);
    private final ErrorAttributes errorAttributes;

    private final UIExceptionHandler uiExceptionHandler;
    private final RestExceptionHandler restExceptionHandler;

    @RequestMapping("/error")
    public Object error(WebRequest request) {
        Map<String, Object> errorAttributesMap = errorAttributes.getErrorAttributes(request, ATTRIBUTE_OPTIONS);
        Throwable error = errorAttributes.getError(request);
        String path = (String) errorAttributesMap.get("path");
        Integer status = (Integer) errorAttributesMap.get("status");
        String msg = (String) errorAttributesMap.get("message");
        return path != null && path.startsWith(SecurityConfig.API_PATH) ?
                restExceptionHandler.processError(error, path, status, msg) :
                uiExceptionHandler.processError(error, path, status, msg);
    }
}
