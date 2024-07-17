package org.ethereum.solidity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

class SolidityTypeTest {

    @Test
    void TestDynamicArrayTypeWithInvalidDataSize() {
        SolidityType.DynamicArrayType dat = new SolidityType.DynamicArrayType("string[]");
        // Not leaving room for any data
        byte[] input = new byte[32];

        input[31] = 0x10; // Indicating we should have 16 elements in the array should fail

        try {
            dat.decode(input, 0);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            // Only acceptable exception
        }

        // Enough room for the length of the array (32b), the length of the first element (32b) and 2 bytes for the actual string ("hi")
        input = new byte[98];

        // how many elements has the array
        input[31] = 0x01;
        // the size of the first element of the array
        input[63] = 0x20; // how many bytes I have to offset
        input[95] = 0x10; // indicating we have 16 characters should fail
        // the actual data
        input[96] = 0x68;
        input[97] = 0x69;

        try {
            dat.decode(input, 0);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            // Only acceptable exception
        }

        // Adding one valid element and the second should fail
        input = new byte[164];

        // how many elements has the array
        input[31] = 0x02;
        // the size of the first element of the array
        input[63] = 0x20; // how many bytes I have to offset
        input[95] = 0x02; // indicating we have 2 characters should work
        // the actual data
        input[96] = 0x68;
        input[97] = 0x69;
        // the size of the second element of the array
        input[129] = 0x20; // how many bytes I have to offset
        input[161] = 0x10; // indicating we have 16 characters should fail
        // the actual data
        input[162] = 0x68;
        input[163] = 0x69;

        try {
            dat.decode(input, 0);
            Assertions.fail();
        } catch (IllegalArgumentException e) {
            // Only acceptable exception
        }


    }

    @Test
    void TestDynamicArrayTypeWithValidDataSize() {
        SolidityType.DynamicArrayType dat = new SolidityType.DynamicArrayType("string[]");

        byte[] input = new byte[231];

        // how many elements has the array
        input[31] = 0x03;
        // the size of the first element of the array
        input[63] = (byte)0x60; // how many bytes I have to offset
        input[159] = 0x02; // indicating we have 2 characters should work
        // the actual data
        input[160] = 0x68;
        input[161] = 0x69;
        // the size of the second element of the array
        input[95] = (byte)0x83; // how many bytes I have to offset
        input[194] = 0x02; // indicating we have 2 characters should work
        // the actual data
        input[195] = 0x69;
        input[196] = 0x68;
        // the size of the third element of the array
        input[127] = (byte)0xA5; // how many bytes I have to offset
        input[228] = 0x02; // indicating we have 2 characters should work
        // the actual data
        input[229] = 0x68;
        input[230] = 0x75;

        Object[] ret = (Object[])dat.decode(input, 0);
        Assertions.assertEquals(3, ret.length);
        Assertions.assertTrue(ret[0].toString().contains("hi"));
        Assertions.assertTrue(ret[1].toString().contains("ih"));
        Assertions.assertTrue(ret[2].toString().contains("hu"));
    }

    @Test
    void TestStaticArrayTypeWithInvalidSize() {
        SolidityType.StaticArrayType dat = new SolidityType.StaticArrayType("string[2]");

        try {
            byte[] input = new byte[34];

            input[31] = 0x02; // indicating we have 2 characters should work
            // the actual data
            input[32] = 0x68;
            input[33] = 0x69;
            dat.decode(input, 0);
            Assertions.fail("should have failed");
        }
        catch (IllegalArgumentException e) {
            // Only acceptable exception
        }

        dat = new SolidityType.StaticArrayType("string[1]");

        try {
            byte[] input = new byte[34];

            input[31] = 0x03; // indicating we have 2 characters should work
            // the actual data
            input[32] = 0x68;
            input[33] = 0x69;
            dat.decode(input, 0);
            Assertions.fail("should have failed");
        }
        catch (IllegalArgumentException e) {
            // Only acceptable exception
        }
    }

    @Test
    void TestStaticArrayType() {
        SolidityType.StaticArrayType dat = new SolidityType.StaticArrayType("string[1]");

        byte[] input = new byte[164];

        input[31] = 0x02; // indicating we have 2 characters should work
        // the actual data
        input[32] = 0x68;
        input[33] = 0x69;

        Object[] ret = dat.decode(input, 0);
        Assertions.assertEquals(1, ret.length);
        Assertions.assertTrue(ret[0].toString().contains("hi"));
    }

    @Test
    void TestIntType() {
        // Should fail, the array is smaller than the offset we define
        try {
            byte[] input = new byte[] {0x4f, 0x4f};
            SolidityType.IntType.decodeInt(input, 12);
            Assertions.fail("should have failed to deserialize the array");
        } catch (IllegalArgumentException e) {
            // Only acceptable exception
        }
        byte[] input = new byte[64];

        // Should get a valid number
        input[31] = 0x01;
        BigInteger value = SolidityType.IntType.decodeInt(input, 0);
        Assertions.assertEquals(1, value.intValue());

        // Should get a valid number
        value = SolidityType.IntType.decodeInt(input, 32);
        Assertions.assertEquals(0, value.intValue());
    }

    @Test
    void TestSafeAddition() {
        // valid additions
        Assertions.assertEquals(0, Math.addExact(0, 0));
        Assertions.assertEquals(2, Math.addExact(1, 1));
        Assertions.assertEquals(1234, Math.addExact(617, 617));
        Assertions.assertEquals(Integer.MAX_VALUE, Math.addExact(Integer.MAX_VALUE - 1, 1));
        Assertions.assertEquals(0, Math.addExact(-1, 1));
        Assertions.assertEquals(-2, Math.addExact(-1, -1));

        // invalid additions
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
            Assertions.fail("should have failed");
        }
        catch (ArithmeticException e) {
            // This is the only exception that this method should throw
        }
    }

}
