package poussecafe.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import poussecafe.util.AbstractBuilder;
import poussecafe.util.ReferenceEquals;

import static poussecafe.check.AssertionSpecification.value;
import static poussecafe.check.Checks.checkThat;

public class AggregateDefinition {

    public Class<?> getEntityClass() {
        return entityClass;
    }

    private Class<?> entityClass;

    public boolean hasFactory() {
        return factoryClass != null;
    }

    @SuppressWarnings("rawtypes")
    public Class getFactoryClass() {
        return factoryClass;
    }

    private Class<?> factoryClass;

    @SuppressWarnings("rawtypes")
    public Class getRepositoryClass() {
        return repositoryClass;
    }

    private Class<?> repositoryClass;

    public boolean hasRepository() {
        return repositoryClass != null;
    }

    public static class Builder extends AbstractBuilder<AggregateDefinition> {

        public Builder() {
            super(new AggregateDefinition());
        }

        public Builder withEntityClass(Class<?> entityClass) {
            product().entityClass = entityClass;
            return this;
        }

        public Builder withFactoryClass(Class<?> factoryClass) {
            product().factoryClass = factoryClass;
            return this;
        }

        public Builder withRepositoryClass(Class<?> repositoryClass) {
            product().repositoryClass = repositoryClass;
            return this;
        }

        @Override
        protected void checkProduct(AggregateDefinition product) {
            checkThat(value(product.entityClass).notNull().because("Entity class cannot be null"));
        }
    }

    private AggregateDefinition() {

    }

    @Override
    public boolean equals(Object obj) {
        return ReferenceEquals.referenceEquals(this, obj).orElse(other -> new EqualsBuilder()
                .append(entityClass, other.entityClass)
                .append(factoryClass, other.factoryClass)
                .append(repositoryClass, other.repositoryClass)
                .build());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(entityClass)
                .append(factoryClass)
                .append(repositoryClass)
                .build();
    }
}
