package de.chrisgw.sportbooking.gui.bind;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Panels;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;


@Data
public abstract class AbstractPropertyInput<T, C extends Interactable> implements PropertyInput<T, C> {

    private final String propertyName;
    private BindingResult bindingResult;


    @Override
    public PropertyValues toPropertyValues() {
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValue(propertyName, getPropertyValue());
        return propertyValues;
    }

    @Override
    public boolean hasFieldErrors() {
        return bindingResult != null && bindingResult.hasFieldErrors(propertyName);
    }

    @Override
    public int getFieldErrorCount() {
        return bindingResult != null ? bindingResult.getFieldErrorCount(propertyName) : 0;
    }

    @Override
    public List<FieldError> getFieldErrors() {
        return bindingResult != null ? bindingResult.getFieldErrors(propertyName) : Collections.emptyList();
    }

    @Override
    public FieldError getFieldError() {
        return bindingResult != null ? bindingResult.getFieldError(propertyName) : null;
    }


    @Override
    public Panel addTo(Panel panel) {
        return Panels.horizontal(getInputComponent()).addTo(panel);
    }


    @Override
    public String toString() {
        return propertyName;
    }

}
