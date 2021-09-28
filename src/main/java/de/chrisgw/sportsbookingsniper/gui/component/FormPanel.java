package de.chrisgw.sportsbookingsniper.gui.component;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.LinearLayout.Alignment;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.googlecode.lanterna.gui2.Direction.VERTICAL;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Beginning;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.Fill;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.CanGrow;
import static com.googlecode.lanterna.gui2.LinearLayout.GrowPolicy.None;
import static com.googlecode.lanterna.gui2.LinearLayout.createLayoutData;
import static org.apache.commons.lang3.StringUtils.trimToNull;


public abstract class FormPanel<V> extends Panel {

    protected final Map<Interactable, Label> fieldToLabelMap = new HashMap<>();


    protected FormPanel() {
        super(new LinearLayout(VERTICAL));
    }


    protected <T extends Interactable> void addFormularField(String labelText, T interactable) {
        interactable.setLayoutData(createLayoutData(Alignment.End, CanGrow));

        Label label = new Label(labelText);
        label.setLayoutData(createLayoutData(Beginning, None));
        fieldToLabelMap.put(interactable, label);

        addComponent(Panels.horizontal(label, interactable), createLayoutData(Fill));
    }

    protected <T extends Component> void addFormularComponent(T component) {
        addComponent(component, createLayoutData(Fill));
    }


    @Override
    protected void onBeforeDrawing() {
        super.onBeforeDrawing();
        adjustLabelSize();
    }

    private void adjustLabelSize() {
        int maxLabelLength = fieldToLabelMap.values()
                .stream()
                .map(Label::getText)
                .mapToInt(String::length)
                .max()
                .orElse(1);
        TerminalSize labelSize = new TerminalSize(maxLabelLength, 1);
        fieldToLabelMap.values().forEach(label -> label.setPreferredSize(labelSize));
    }


    protected boolean isEmpty(TextBox textBox) {
        boolean hasError = StringUtils.isEmpty(trimToNull(textBox.getText()));
        setFieldFeedback(textBox, hasError);
        return hasError;
    }

    protected <T> boolean isUnselected(ComboBox<T> comboBox) {
        boolean hasError = comboBox.getSelectedItem() == null;
        setFieldFeedback(comboBox, hasError);
        return hasError;
    }

    protected <T> boolean isUnselected(CheckBoxList<T> checkBoxList) {
        boolean hasError = checkBoxList.getCheckedItems().isEmpty();
        setFieldFeedback(checkBoxList, hasError);
        return hasError;
    }


    protected void setFieldFeedback(Interactable interactable, boolean hasError) {
        Label label = fieldToLabelMap.get(interactable);
        if (label != null && hasError) {
            label.setForegroundColor(ANSI.RED).setBackgroundColor(ANSI.WHITE);
        } else if (label != null) {
            label.setForegroundColor(null).setBackgroundColor(null);
        }
    }


    public abstract V readFormValue();

    /**
     * @return {@code true} if this {@link FormPanel} contains any validation errors, otherwise {@code false}
     */
    public abstract boolean validateForm();


    public abstract void setFormValue(V formValue);

    public void resetFormValue() {
        setFormValue(null);
    }

}
