package poussecafe.context;

import java.util.Map;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public abstract class DependencyInjector {

    private Map<Class<?>, Object> injectableServices;

    protected DependencyInjector(Map<Class<?>, Object> injectableServices) {
        setInjectableServices(injectableServices);
    }

    private void setInjectableServices(Map<Class<?>, Object> injectableServices) {
        checkThat(value(injectableServices).notNull().because("Injectable services cannot be null"));
        this.injectableServices = injectableServices;
    }

    public void trySettingDependency() {
        if (isValidTarget()) {
            setDependencyIfResolved();
        }
    }

    protected abstract boolean isValidTarget();

    private void setDependencyIfResolved() {
        Class<?> dependencyType = getTargetType();
        Object dependency = injectableServices.get(dependencyType);
        if (dependency != null) {
            setResolvedDependency(dependency);
        }
    }

    protected abstract Class<?> getTargetType();

    protected abstract void setResolvedDependency(Object dependency);
}
