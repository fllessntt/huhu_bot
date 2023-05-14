package tech.chowyijiu.huhu_bot.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author elastic chow
 * @date 14/5/2023
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BotPlugin {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}