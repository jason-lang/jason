package jason.stdlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/** Manual for internal actions */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE) //on class level
public @interface Manual {
    String    hint()     default "no hint"; 
    String    literal()  default "noliteral(a,b,c)"; 
    String[]  argsHint() default ""; 
    String[]  argsType() default "";
}
