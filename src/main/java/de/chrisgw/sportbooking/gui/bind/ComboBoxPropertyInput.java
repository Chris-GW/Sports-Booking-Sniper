package de.chrisgw.sportbooking.gui.bind;

import com.googlecode.lanterna.gui2.ComboBox;

import static java.util.Objects.requireNonNull;


public class ComboBoxPropertyInput<T> extends AbstractPropertyInput<T, ComboBox<T>> {

    private final ComboBox<T> comboBox;


    public ComboBoxPropertyInput(String propertyName, ComboBox<T> comboBox) {
        super(propertyName);
        this.comboBox = requireNonNull(comboBox);
    }


    @Override
    public ComboBox<T> getInputComponent() {
        return comboBox;
    }

    @Override
    public void setPropertyValue(T propertyValue) {
        comboBox.setSelectedItem(propertyValue);
    }

    @Override
    public T getPropertyValue() {
        return comboBox.getSelectedItem();
    }

}
