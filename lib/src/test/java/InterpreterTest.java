import interpreter.Interpreter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

public class InterpreterTest {

    @Test
    void testSimpliestProgram() {
        String code = "out 44.0 + 56";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Double.doubleToLongBits(100.0),
                Double.doubleToLongBits(
                        Double.parseDouble(interpreterOutput.output)
                )
        );
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgram() {
        double number = 42.0;
        String code =
                "var count = " + number + "\n" +
                        "out count";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Double.doubleToLongBits(number),
                Double.doubleToLongBits(
                        Double.parseDouble(interpreterOutput.output)
                )
        );
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithSyntaxError() {
        String code =
                "var count = ," +
                        "out count";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithExpression() {
        String expr = "42+21+11.5";
        String code =
                "var expr = " + expr + "\n" +
                        "out expr";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        // TODO: Test result of expr, not the expr.
        Assertions.assertEquals("74.5", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithValidSequence() {
        String code =
                "var seq = {21, 42 }\n" +
                        "out seq";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(getSequence(21, 42)), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithWrongOrderSequence() {
        String seq = "21, 1";
        String code =
                "var seq = {" + seq + "}\n" +
                        "out seq";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithFloatElementSequence() {
        String seq = "{1.2, 21}";
        String code =
                "var seq = " + seq + "\n" +
                        "out seq";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithExpressionElementSequence() {
        String seq = "{1 + 2, 21}";
        String code =
                "var seq = " + seq + "\n" +
                        "out seq";


        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(getSequence(1 + 2, 21)), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSequenceAndExpressionProgram() {
        String code =
                "var seq = {1 + 2, 21} \n" +
                        "var consts = 2.7 * 3.14\n" +
                        "out consts";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.startsWith("8.478"));
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMultipleExpressions() {
        String code =
                "var foo = 1 + 2\n" +
                        "var bar = 3 ^ 2 * 3\n" +
                        "out foo + bar";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("30.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testInvalidExpressionInOutput() {
        String code =
                "var foo = {1,2}\n" +
                        "var bar = 3 ^ 2 * 3\n" +
                        "out foo + bar";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
//        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testInvalidExpression() {
        String code =
                "var foo = {1,2}\n" +
                        "var bar = 10090912 + 0.000912131\n" +
                        "var foobar = foo + bar";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSumOfN() {
        String code =
                "var a = 1\n" +
                        "var b = 10\n" +
                        "var sum = 10 * (a + b) / 2\n" +
                        "out sum + 0";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }


    @Test
    void testMultipleOut() {
        String code =
                "var a = 1\n" +
                        "var b = 10\n" +
                        "out a + b\n" +
                        "var sum = 10 * (a + b) / 2\n" +
                        "out sum + 0";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("11.0\n55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testPrint() {
        String code =
                "10 * (1 + 10) / 2\n" +
                        "print 55.0";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("55.0", interpreterOutput.output);
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testPrintAndOut() {
        String code =
                "var o = 10 * (1 + 10) / 2\n" +
                "print \"o is\"\n" +
                "out o";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("\"o is\"\n55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMap() {
        String code =
                "var squares = map({1,5}, i -> i^2 * 3)\n" +
                        "out squares";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                1 * 3, 2 * 2 * 3, 3 * 3 * 3, 4 * 4 * 3, 5 * 5 * 3
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMapOut() {
        String code = "out map({1,5}, i -> i^2)";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                1, 2 * 2, 3 * 3, 4 * 4, 5 * 5
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMapVariable() {
        String code =
                "var seq = {1,5}\n" +
                        "var squares = map( seq, i -> i^2)\n" +
                        "out squares";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                1, 2 * 2, 3 * 3, 4 * 4, 5 * 5
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMapAndExpression() {
        String code = "var seq = {1,5}\n" +
                "var squares = map(seq, i -> i^2)\n" +
                "out squares\n" +
                "var o = 10 * (1 + 10) / 2\n" +
                "out o";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                1, 2 * 2, 3 * 3, 4 * 4, 5 * 5
        }) + "\n55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMultipleMapApplies() {
        String code = "var seq = {1,3}\n" +
                "var doubles = map(seq, i->  i + i)\n" +
                "out doubles\n" +
                "var squares = map(doubles, i   -> i * i)\n" +
                "out squares";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("[2.0, 4.0, 6.0]\n[4.0, 16.0, 36.0]", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    // TODO: correctly reset variables
    @Test
    void testInnerMap() {
        String code = "var seq = {1,3}\n" +
                "var ddoubles = map(map(seq, i -> i + i), i -> i + i)\n" +
                "out map(map(seq, i -> i + i), i -> i + i)\n";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                4, 8, 12
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testMore() {
        String code = "var n = 5\n" +
                "out map({1, n}, i -> (-1)^i / (2 * i + 1))\n";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                -0.3333333333333333, 0.2, -0.14285714285714285, 0.1111111111111111, -0.09090909090909091
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    private double[] getSequence(int from, int to) {
        double[] out = new double[to - from + 1];

        for (int i = from; i <= to; i++) {
            out[i - from] = i;
        }

        return out;
    }
}
