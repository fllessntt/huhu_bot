package tech.chowyijiu.huhu_bot.exception;

/**
 * @author elastic chow
 * @date 25/7/2023
 */
public class IllegalDataException extends RuntimeException{

    public IllegalDataException(String message) {
        super(message);
    }

    public IllegalDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
