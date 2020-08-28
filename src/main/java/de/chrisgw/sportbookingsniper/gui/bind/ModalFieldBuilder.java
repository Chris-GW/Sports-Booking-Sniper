package de.chrisgw.sportbookingsniper.gui.bind;

import com.googlecode.lanterna.gui2.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.defaultString;


public final class ModalFieldBuilder<I extends Interactable, T> {

    private String propertyName;
    private Label label = new Label("");
    private Label errorLabel = new Label("");

    private I inputField;
    private Function<I, T> inputFieldReader;
    private BiConsumer<I, T> inputFieldWriter;

    private ModalFieldBuilder(String propertyName) {
        this.propertyName = propertyName;
        String defaultLabelText = StringUtils.capitalize(propertyName) + ":*";
        withLabel(defaultLabelText);
    }


    public static ModalFieldBuilder<TextBox, String> newTextBoxField(String propertyName) {
        ModalFieldBuilder<TextBox, String> modalFieldBuilder = new ModalFieldBuilder<>(propertyName);
        modalFieldBuilder.inputField = new TextBox();
        modalFieldBuilder.inputFieldReader = TextBox::getText;
        modalFieldBuilder.inputFieldReader = modalFieldBuilder.inputFieldReader.andThen(StringUtils::trimToNull);
        modalFieldBuilder.inputFieldWriter = (textBox, propertyValue) -> textBox.setText(defaultString(propertyValue));
        return modalFieldBuilder;
    }


    public static <T> ModalFieldBuilder<RadioBoxList<T>, T> newRadioBoxField(String propertyName) {
        ModalFieldBuilder<RadioBoxList<T>, T> modalFieldBuilder = new ModalFieldBuilder<>(propertyName);
        modalFieldBuilder.inputField = new RadioBoxList<>();
        modalFieldBuilder.inputFieldReader = RadioBoxList::getCheckedItem;
        modalFieldBuilder.inputFieldWriter = RadioBoxList::setCheckedItem;
        return modalFieldBuilder;
    }


    public static <T> ModalFieldBuilder<ComboBox<T>, T> newComboBoxField(String propertyName) {
        return newComboBoxField(propertyName, Collections.emptyList());
    }

    public static <T> ModalFieldBuilder<ComboBox<T>, T> newComboBoxField(String propertyName, T[] items) {
        return newComboBoxField(propertyName, Arrays.asList(items));
    }

    public static <T> ModalFieldBuilder<ComboBox<T>, T> newComboBoxField(String propertyName, Collection<T> items) {
        ModalFieldBuilder<ComboBox<T>, T> modalFieldBuilder = new ModalFieldBuilder<>(propertyName);
        modalFieldBuilder.inputField = new ComboBox<>(items);
        modalFieldBuilder.inputFieldReader = ComboBox::getSelectedItem;
        modalFieldBuilder.inputFieldWriter = ComboBox::setSelectedItem;
        return modalFieldBuilder;
    }


    public ModalFieldBuilder<I, T> withPropertyName(String propertyName) {
        this.propertyName = requireNonNull(propertyName);
        return this;
    }


    public ModalFieldBuilder<I, T> withLabel(String labelText) {
        this.label.setText(labelText);
        return this;
    }

    public ModalFieldBuilder<I, T> withLabel(Label label) {
        this.label = requireNonNull(label);
        return this;
    }

    public ModalFieldBuilder<I, T> withErrorLabel(Label errorLabel) {
        this.errorLabel = requireNonNull(errorLabel);
        return this;
    }


    public ModalFieldBuilder<I, T> withInputField(I inputField) {
        this.inputField = inputField;
        return this;
    }


    public ModalField<I, T> build() {
        errorLabel.setVisible(false);
        return new ModalField<>(propertyName, label, errorLabel, inputField, inputFieldWriter,
                inputFieldReader);
    }

    public ModalField<I, T> addTo(ModalForm modalForm) {
        ModalField<I, T> modalField = build();
        modalForm.addModalField(modalField);
        return modalField;
    }

}
