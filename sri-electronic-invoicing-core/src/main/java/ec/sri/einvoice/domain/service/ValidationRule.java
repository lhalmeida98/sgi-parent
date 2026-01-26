package ec.sri.einvoice.domain.service;

public interface ValidationRule<T> {
  ValidationResult validate(T target);
}
