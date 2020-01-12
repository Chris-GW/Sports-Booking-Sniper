package de.chrisgw.sportbooking.gui.bind;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;


public interface PropertyInput<T, C extends Interactable> {

    String getPropertyName();
    void setPropertyValue(T propertyValue);
    T getPropertyValue();
    PropertyValues toPropertyValues();

    C getInputComponent();


    Panel addTo(Panel panel);



    boolean hasFieldErrors();
    int getFieldErrorCount();
    List<FieldError> getFieldErrors();
    FieldError getFieldError();

    BindingResult getBindingResult();
    void setBindingResult(BindingResult bindingResult);

}
