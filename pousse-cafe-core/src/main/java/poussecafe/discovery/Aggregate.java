package poussecafe.discovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import poussecafe.domain.Factory;
import poussecafe.domain.Module;
import poussecafe.domain.Repository;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("rawtypes")
public @interface Aggregate {

    Class<? extends Factory> factory();

    Class<? extends Repository> repository();

    Class<? extends Module> module() default DefaultModule.class;
}
