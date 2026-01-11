package br.com.tiagoolileite.productcatalog.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private final String field;
    private final transient Object rejectedValue;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
    }

    public ValidationException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}
