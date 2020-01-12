package de.chrisgw.sportbooking.gui.bind;

import com.googlecode.lanterna.gui2.TextBox;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public class TextBoxPropertyInput extends AbstractPropertyInput<String, TextBox> {

    private final TextBox textBox;


    public TextBoxPropertyInput(String propertyName, TextBox textBox) {
        super(propertyName);
        this.textBox = textBox;
    }


    @Override
    public TextBox getInputComponent() {
        return textBox;
    }

    @Override
    public void setPropertyValue(String propertyValue) {
        textBox.setText(defaultString(propertyValue));
    }

    @Override
    public String getPropertyValue() {
        return trimToNull(textBox.getText());
    }

}
