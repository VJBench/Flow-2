package com.vaadin.flow.data.value;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

public class ValueChangeModeTest {

    @Test
    public void implementHasValueChangeMode_noPropertiesSynchronizedInitially() {

        ValueChangeModeComponent component = new ValueChangeModeComponent();

        Assert.assertTrue(
                "Initially there should be no synchronized properties",
                component.getElement().getSynchronizedProperties()
                        .count() == 0);
        Assert.assertTrue(
                "Initially there should be no synchronized property-events",
                component.getElement().getSynchronizedPropertyEvents()
                        .count() == 0);
    }

    @Test
    public void changingValueChangeMode_onlyCorrespondingPropertySynchronized() {

        ValueChangeModeComponent component = new ValueChangeModeComponent();
        setAndAssertValueChangeModeEager(component);
        setAndAssertValueChangeModeOnChange(component);
        setAndAssertValueChangeModeOnBlur(component);

        component = new ValueChangeModeComponent();
        setAndAssertValueChangeModeOnChange(component);
        setAndAssertValueChangeModeOnBlur(component);
        setAndAssertValueChangeModeEager(component);

        component = new ValueChangeModeComponent();
        setAndAssertValueChangeModeOnBlur(component);
        setAndAssertValueChangeModeEager(component);
        setAndAssertValueChangeModeOnChange(component);
    }

    @Test
    public void changingValueChangeModeToNull_noPropertiesSynchronized() {

        ValueChangeModeComponent component = new ValueChangeModeComponent();
        component.setValueChangeMode(ValueChangeMode.EAGER);

        component.setValueChangeMode(null);

        Assert.assertTrue(
                "Component should not have synchronized properties after setting value change mode to null",
                component.getElement().getSynchronizedProperties()
                        .count() == 0);
        Assert.assertTrue(
                "Component should not have synchronized property-events after setting value change mode to null",
                component.getElement().getSynchronizedPropertyEvents()
                        .count() == 0);

    }

    @Tag("tag")
    private static class ValueChangeModeField
            extends AbstractSinglePropertyField<ValueChangeModeField, String>
            implements HasValueChangeMode<ValueChangeModeField, String> {
        private ValueChangeMode valueChangeMode;

        public ValueChangeModeField() {
            super("value", "", false);
        }

        @Override
        public void setValueChangeMode(ValueChangeMode valueChangeMode) {
            this.valueChangeMode = valueChangeMode;

            setSynchronizedEvent(ValueChangeMode.eventForMode(valueChangeMode,
                    "value-changed"));
        }

        @Override
        public ValueChangeMode getValueChangeMode() {
            return valueChangeMode;
        }
    }

    @Test
    public void field_setMode() {
        ValueChangeModeField field = new ValueChangeModeField();

        field.setValueChangeMode(ValueChangeMode.ON_BLUR);
        assertValueSynchronizedWithEvent(field, "blur");

        field.setValueChangeMode(null);
        Assert.assertEquals(0,
                field.getElement().getSynchronizedPropertyEvents().count());

        field.setValueChangeMode(ValueChangeMode.EAGER);
        assertValueSynchronizedWithEvent(field, "value-changed");
    }

    private void setAndAssertValueChangeModeEager(
            ValueChangeModeComponent component) {

        component.setValueChangeMode(ValueChangeMode.EAGER);
        assertValueSynchronizedWithEvent(component,
                component.getClientPropertyChangeEventName());
    }

    private void setAndAssertValueChangeModeOnChange(
            ValueChangeModeComponent component) {

        component.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        assertValueSynchronizedWithEvent(component, "change");
    }

    private void setAndAssertValueChangeModeOnBlur(
            ValueChangeModeComponent component) {

        component.setValueChangeMode(ValueChangeMode.ON_BLUR);
        assertValueSynchronizedWithEvent(component, "blur");
    }

    private void assertValueSynchronizedWithEvent(Component component,
            String eventName) {

        Assert.assertArrayEquals(
                "value should be the only synchronized property",
                new String[] { "value" }, component.getElement()
                        .getSynchronizedProperties().toArray(String[]::new));

        String[] syncedPropertyEvents = component.getElement()
                .getSynchronizedPropertyEvents().toArray(String[]::new);
        String[] expectedPropertyEvents = { eventName };

        Assert.assertArrayEquals(
                eventName + " should be the only synchronized property-event",
                expectedPropertyEvents, syncedPropertyEvents);
    }

}
