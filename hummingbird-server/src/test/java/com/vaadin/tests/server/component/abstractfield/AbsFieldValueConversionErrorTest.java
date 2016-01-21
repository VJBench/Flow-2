package com.vaadin.tests.server.component.abstractfield;

import org.junit.Assert;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.tests.data.bean.Address;
import com.vaadin.tests.data.bean.Country;
import com.vaadin.tests.data.bean.Person;
import com.vaadin.tests.data.bean.Sex;
import com.vaadin.ui.TestField;

import junit.framework.TestCase;

public class AbsFieldValueConversionErrorTest extends TestCase {

    Person paulaBean = new Person("Paula", "Brilliant", "paula@brilliant.com",
            34, Sex.FEMALE,
            new Address("Paula street 1", 12345, "P-town", Country.FINLAND));

    public void testValidateConversionErrorParameters() {
        TestField tf = new TestField();
        tf.setConverter(new StringToIntegerConverter());
        tf.setPropertyDataSource(new MethodProperty<String>(paulaBean, "age"));
        tf.setConversionError("(Type: {0}) Converter exception message: {1}");
        tf.setValue("abc");
        try {
            tf.validate();
            fail();
        } catch (InvalidValueException e) {
            Assert.assertEquals(
                    "(Type: Integer) Converter exception message: Could not convert 'abc' to java.lang.Integer",
                    e.getMessage());
        }

    }

    public void testConvertToModelConversionErrorParameters() {
        TestField tf = new TestField();
        tf.setConverter(new StringToIntegerConverter());
        tf.setPropertyDataSource(new MethodProperty<String>(paulaBean, "age"));
        tf.setConversionError("(Type: {0}) Converter exception message: {1}");
        tf.setValue("abc");
        try {
            tf.getConvertedValue();
            fail();
        } catch (ConversionException e) {
            Assert.assertEquals(
                    "(Type: Integer) Converter exception message: Could not convert 'abc' to java.lang.Integer",
                    e.getMessage());
        }

    }

    public void testNullConversionMessages() {
        TestField tf = new TestField();
        tf.setConverter(new StringToIntegerConverter());
        tf.setPropertyDataSource(new MethodProperty<String>(paulaBean, "age"));
        tf.setConversionError(null);
        tf.setValue("abc");
        try {
            tf.validate();
            fail();
        } catch (InvalidValueException e) {
            Assert.assertEquals(null, e.getMessage());
        }

    }

    public void testDefaultConversionErrorMessage() {
        TestField tf = new TestField();
        tf.setConverter(new StringToIntegerConverter());
        tf.setPropertyDataSource(new MethodProperty<String>(paulaBean, "age"));
        tf.setValue("abc");

        try {
            tf.validate();
            fail();
        } catch (InvalidValueException e) {
            Assert.assertEquals("Could not convert value to Integer",
                    e.getMessage());
        }

    }
}