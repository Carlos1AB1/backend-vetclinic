package com.vetclinic.patterns.chain;

/**
 * Chain of Responsibility Pattern
 * Base handler for validation chain
 */
public abstract class ValidationHandler<T> {
    
    protected ValidationHandler<T> next;

    public ValidationHandler<T> setNext(ValidationHandler<T> next) {
        this.next = next;
        return next;
    }

    public abstract ValidationResult validate(T request);

    protected ValidationResult checkNext(T request) {
        if (next == null) {
            return ValidationResult.success();
        }
        return next.validate(request);
    }
}
