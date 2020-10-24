import calculator.Calculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorTest {

    private static final String[] EXPRESSIONS = {
            "42 + 21 + 11.5",
            "700 - 200 * 3",
            "3 + 4 * ( 2 - 1 )",
            "( 1.0 + 2 ) * 4 + 3",
            "(  2 + 3.0 ) * 11  +  1",
            "2 ^ 2 ^ 2",
            "3 * 2 + 5",
            "( 12 - 3 ) / 3",
            "5 +  2 ^ 3",
            "3.2 * 2 - 11",
            "2 + 1 - 12 / 3",
            "( 6 - 3 ) ^ 2 - 11",
            "6 - 3 ^ 2 - 11",
            "162 / ( 2 + 1 ) ^ 4",
            "( 3 + 5 ) * ( 7 - 2 )"};

    private static final String[] CONVERTED_EXPRESSIONS = {
            "42 21 + 11.5 +",
            "700 200 3 * -",
            "3 4 2 1 - * +",
            "1.0 2 + 4 * 3 +",
            "2 3.0 + 11 * 1 +",
            "2 2 2 ^ ^",
            "3 2 * 5 +",
            "12 3 - 3 /",
            "5 2 3 ^ +",
            "3.2 2 * 11 -",
            "2 1 + 12 3 / -",
            "6 3 - 2 ^ 11 -",
            "6 3 2 ^ - 11 -",
            "162 2 1 + 4 ^ /",
            "3 5 + 7 2 - *"
    };

    private static final double[] RESULTS = {
            74.5,
            100,
            7,
            15.0,
            56,
            16,
            11,
            3,
            13,
            -4.6,
            -1,
            -2,
            -14,
            2,
            40
    };

    @Test
    void testCalculator() {
        int size = EXPRESSIONS.length;
        Calculator calculator = new Calculator();
        for (int i = 0; i < size; i++) {
//            String converted = calculator.convert(EXPRESSIONS[i]);
//            System.out.println(converted);
//            Assertions.assertEquals(converted, CONVERTED_EXPRESSIONS[i]);

            double result = calculator.calc(EXPRESSIONS[i]);
//            System.out.println("Expected result: " + RESULTS[i] + "; Actual result: " + result);
            Assertions.assertEquals(Double.doubleToLongBits(result), Double.doubleToLongBits(RESULTS[i]));
        }
    }
}
