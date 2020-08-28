package de.chrisgw.sportbookingsniper.gui.bind;

import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import lombok.Data;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.googlecode.lanterna.gui2.GridLayout.createHorizontallyFilledLayoutData;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Data
public class ModalField<I extends Interactable, T> {

    private final String propertyName;
    private final Label label;
    private final Label errorLabel;
    private final ConcealableComponent concealableErrorLabel;

    private final I inputField;
    private final BiConsumer<I, T> inputFieldWriter;
    private final Function<I, T> inputFieldReader;
    private BindingResult bindingResult;


    public T readProperty() {
        return inputFieldReader.apply(inputField);
    }

    public void writeProperty(T propertyValue) {
        inputFieldWriter.accept(inputField, propertyValue);
    }


    public void setBindingResult(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        if (bindingResult.hasFieldErrors(propertyName)) {
            label.setForegroundColor(ANSI.WHITE).setBackgroundColor(ANSI.RED);
            setErrorMessage(bindingResult.getFieldError(propertyName));
        } else {
            label.setForegroundColor(null).setBackgroundColor(null);
            errorLabel.setText("");
        }
    }

    private void setErrorMessage(FieldError fieldError) {
        String errorMessage = defaultString(fieldError.getDefaultMessage());
        errorLabel.setText(errorMessage);
        concealableErrorLabel.setVisible(isNotEmpty(errorMessage));
    }


    public ModalField<I, T> addToGrid(Panel panel) {
        panel.addComponent(getLabel());
        panel.addComponent(getInputField(), createHorizontallyFilledLayoutData(1));
        panel.addComponent(getConcealableErrorLabel(), createHorizontallyFilledLayoutData(2));
        return this;
    }

}
