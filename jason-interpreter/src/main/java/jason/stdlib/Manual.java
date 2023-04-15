package jason.stdlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;

/** Manual for internal actions */
@Target(ElementType.TYPE) //on class level
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Manual {
    String    hint()     default "no hint";
    String    literal()  default "noliteral(a,b,c)";
    String[]  argsHint() default "";
    String[]  argsType() default "";
    String[]  examples() default "";
    String[]  seeAlso()  default "";
}
