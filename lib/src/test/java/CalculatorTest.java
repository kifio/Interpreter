import calculator.Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorTest {

    private static class TestData {
        String expression;
        String converted;
        double result;

        TestData(String expression, String converted, double result) {
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
                    100
            ),
            new TestData(
                    "3 + 4 * ( 2 - 1 )",
                    "3 4 2 1 - * +",
                    7
            ),
            new TestData(
                    "( 1.0 + 2 ) * 4 + 3",
                    "1.0 2 + 4 * 3 +",
                    15.0
            ),
            new TestData(
                    "(  2 + 3.0 ) * 11  +  1",
                    "2 3.0 + 11 * 1 +",
                    56
            ),
            new TestData(
                    "2 ^ 2 ^ 2",
                    "2 2 2 ^ ^",
                    16
            ),
            new TestData(
                    "3 * 2 + 5",
                    "3 2 * 5 +",
                    11
            ),
            new TestData(
                    "( 12 - 3 ) / 3",
                    "12 3 - 3 /",
                    3
            ),
            new TestData(
                    "5 +  2 ^ 3",
                    "5 2 3 ^ +",
                    13
            ),
            new TestData(
                    "3.2 * 2 - 11",
                    "3.2 2 * 11 -",
                    -4.6
            ),
            new TestData(
                    "2 + 1 - 12 / 3",
                    "2 1 + 12 3 / -",
                    -1
            ),
            new TestData(
                    "( 6 - 3 ) ^ 2 - 11",
                    "6 3 - 2 ^ 11 -",
                    -2
            ),
            new TestData(
                    "6 - 3 ^ 2 - 11",
                    "6 3 2 ^ - 11 -",
                    -14)
            ,
            new TestData(
                    "162 / ( 2 + 1 ) ^ 4",
                    "162 2 1 + 4 ^ /",
                    2
            ),
            new TestData(
                    "( 3 + 5 ) * ( 7 - 2 )",
                    "3 5 + 7 2 - *",
                    40
            )
    };


    @Test
    void testCalculator() {
        Calculator calculator = new Calculator();
        for (TestData testDatum : TEST_DATA) {
            double result = calculator.calc(testDatum.expression);
            System.out.println("actual: " + result + "; expected: " + testDatum.result);
            Assertions.assertEquals(
                    Double.doubleToLongBits(result),
                    Double.doubleToLongBits(testDatum.result)
            );
        }
    }
}
