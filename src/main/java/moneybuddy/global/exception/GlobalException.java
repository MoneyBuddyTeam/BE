package moneybuddy.global.exception;

import lombok.Getter;
import moneybuddy.global.enums.ErrorCode;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GlobalException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
