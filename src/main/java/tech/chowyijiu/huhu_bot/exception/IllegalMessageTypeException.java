package tech.chowyijiu.huhu_bot.exception;

/**
 * @author elastic chow
 * @date 26/7/2023
 */
public class IllegalMessageTypeException extends RuntimeException {

    public IllegalMessageTypeException() {
        super("The type of message must be one of the String, MessageSegment, Message");
    }

    public IllegalMessageTypeException(Throwable cause) {
        super("The type of message must be one of the String, MessageSegment, Message", cause);
    }
}
