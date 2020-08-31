package de.chrisgw.sportsbookingsniper.gui.bind;

import com.googlecode.lanterna.gui2.Interactable;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.util.LinkedHashMap;
import java.util.Map;


public class ModalForm {

    private Map<String, ModalField> modalFields = new LinkedHashMap<>();


    public BindingResult bindData(DataBinder dataBinder) {
        dataBinder.bind(readPropertyValues());
        dataBinder.validate();
        BindingResult bindingResult = dataBinder.getBindingResult();
        modalFields.values().forEach(modalField -> modalField.setBindingResult(bindingResult));
        return bindingResult;
    }


    public void addModalField(ModalField modalField) {
        String propertyName = modalField.getPropertyName();
        modalFields.put(propertyName, modalField);
    }


    public ModalField getModalField(String propertyName) {
        return modalFields.get(propertyName);
    }

    public <I extends Interactable> I getModalFieldInput(String propertyName, Class<I> inputClazz) {
        Interactable interactable = modalFields.get(propertyName).getInputField();
        return inputClazz.cast(interactable);
    }

    public <T> T getModalFieldValue(String propertyName, Class<T> valueClazz) {
        return valueClazz.cast(modalFields.get(propertyName).readProperty());
    }


    public PropertyValues readPropertyValues() {
        return modalFields.values()
                .stream()
                .map(modalField -> new PropertyValue(modalField.getPropertyName(), modalField.readProperty()))
                .reduce(new MutablePropertyValues(), MutablePropertyValues::addPropertyValue,
                        MutablePropertyValues::addPropertyValues);
    }

    public void writePropertyValues(PropertyValues propertyValues) {
        for (PropertyValue propertyValue : propertyValues) {
            String propertyName = propertyValue.getName();
            ModalField modalField = getModalField(propertyName);
            if (modalField != null) {
                modalField.writeProperty(propertyValue.getValue());
            } else {
                System.out.println("unknown property: " + propertyName);
            }
        }
    }

}
