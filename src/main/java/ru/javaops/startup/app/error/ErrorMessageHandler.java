package ru.javaops.startup.app.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorMessageHandler {
    @Getter
    private final MessageSource messageSource;

    public List<String> getErrorList(BindingResult result) {
        Map<String, String> errorMap = getErrorMap(result);
        return errorMap.entrySet().stream()
                .map(entry -> String.format("Field '%s': %s", entry.getKey(), entry.getValue()))
                .toList();
    }

    public Map<String, String> getErrorMap(BindingResult result) {
        Map<String, String> invalidParams = new LinkedHashMap<>();
        for (ObjectError error : result.getGlobalErrors()) {
            invalidParams.put(error.getObjectName(), getErrorMessage(error));
        }
        for (FieldError error : result.getFieldErrors()) {
            invalidParams.put(error.getField(), getErrorMessage(error));
        }
        return invalidParams;
    }

    private String getErrorMessage(ObjectError error) {
        return error.getCode() == null ? error.getDefaultMessage() :
                messageSource.getMessage(error.getCode(), error.getArguments(), error.getDefaultMessage(), LocaleContextHolder.getLocale());
    }
}
