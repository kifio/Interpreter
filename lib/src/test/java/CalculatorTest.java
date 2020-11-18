import calculator.Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class CalculatorTest {

    private static class TestData {
        final String expression;
        final String converted;
        final Double result;

        TestData(String expression, String converted, Double result) {
            this.expression = expression;
            this.converted = converted;
            this.result = result;
        }

        @Override
        public String toString() {
            return "TestData{" +
                    "expression='" + expression + '\'' +
                    ", converted='" + converted + '\'' +
                    ", result=" + result +
                    '}';
        }
    }

    private static final TestData[] TEST_DATA = {
            new TestData(
                    "(-42) + 21 + 11.5",
                    "42 - 21 + 11.5 +",
                    -9.5
            ),
            new TestData(
                    "700 - 200 * 3",
                    "700 200 3 * -",
                    100.0
            ),
            new TestData(
                    "3 + 4 * ( 2 - 1 )",
                    "3 4 2 1 - * +",
                    7.0
            ),
            new TestData(
                    "( 1.0 + 2 ) * 4 + 3",
                    "1.0 2 + 4 * 3 +",
                    15.0
            ),
            new TestData(
                    "(  2 + 3.0 ) * 11  +  1",
                    "2 3.0 + 11 * 1 +",
                    56.0
            ),
            new TestData(
                    "2 ^ 2 ^ 2",
                    "2 2 2 ^ ^",
                    16.0
            ),
            new TestData(
                    "3 * 2 + 5",
                    "3 2 * 5 +",
                    11.0
            ),
            new TestData(
                    "( 12 - 3 ) / 3",
                    "12 3 - 3 /",
                    3.0
            ),
            new TestData(
                    "5 +  2 ^ 3",
                    "5 2 3 ^ +",
                    13.0
            ),
            new TestData(
                    "3.2 * 2 - 11",
                    "3.2 2 * 11 -",
                    -4.6
            ),
            new TestData(
                    "2 + 1 - 12 / 3",
                    "2 1 + 12 3 / -",
                    -1.0
            ),
            new TestData(
                    "( 6 - 3 ) ^ 2 - 11",
                    "6 3 - 2 ^ 11 -",
                    -2.0
            ),
            new TestData(
                    "6 - 3 ^ 2 - 11",
                    "6 3 2 ^ - 11 -",
                    -14.0
            ),
            new TestData(
                    "162 / ( 2 + 1 ) ^ 4",
                    "162 2 1 + 4 ^ /",
                    2.0
            ),
            new TestData(
                    "( 3 + 5 ) * ( 7 - 2 )",
                    "3 5 + 7 2 - *",
                    40.0
            )
    };

    private static final TestData[] BAD_TEST_DATA = {
            new TestData(
                    "{ 100",
                    null,
                    null
            ),
            new TestData(
                    "100 +-",
                    null,
                    null
            ),
    };


    @Test
    void testCalculator() {
        Calculator calculator = new Calculator();

        for (TestData testDatum : TEST_DATA) {
            Double result = calculator.calc(testDatum.expression, Collections.emptyMap());
            System.out.println("actual: " + result + "; expected: " + testDatum.result);
            Assertions.assertEquals(
                    Double.doubleToLongBits(result),
                    Double.doubleToLongBits(testDatum.result)
            );
        }

        for (TestData testDatum : BAD_TEST_DATA) {
            Assertions.assertNull(calculator.calc(testDatum.expression, Collections.emptyMap()));
        }
    }
}
