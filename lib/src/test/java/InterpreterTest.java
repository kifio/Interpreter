import interpreter.Interpreter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class InterpreterTest {

    @Test
    void testSingleLinePrograms() {
        Interpreter.Output interpreterOutput = new Interpreter().interpret("out 44.0 + 56");
        Assertions.assertEquals("100.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());


        interpreterOutput = new Interpreter().interpret("out 44.0 + 56)");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        interpreterOutput = new Interpreter().interpret("out (44.0 + 56");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        interpreterOutput = new Interpreter().interpret("(+ 56");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        interpreterOutput = new Interpreter().interpret("out (56");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        interpreterOutput = new Interpreter().interpret("out 56)");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret("out 56 + )");
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());

    }

    @Test
    void testSimpleProgram() {
        String code =
                "var count = 42\n" +
                        "out count";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals("42.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());


        code = "var count = 44.0 + 56)\n" +
                "out count";
        interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        code = "var count = (44.0 + 56\n" +
                "out count";
        interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        code = "var count = (44.0 + 56\n" +
                "out count";
        interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        code = "var count = ( + \n" +
                "out count";
        interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());


        code = "var count = ( ) \n" +
                "out count";
        interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());

    }

    @Test
    void testSimpleProgramWithSyntaxError() {
        Interpreter.Output interpreterOutput = new Interpreter().interpret("var count = ,\n" +
                "out count");
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithExpression() {
        Interpreter.Output interpreterOutput = new Interpreter().interpret(
                "var expr = 42+21+11.5\n" +
                        "out expr");
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
    void testSimpleProgramWithDoubleElementSequence() {
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
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
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
    void testPrintAndOut() {
        Interpreter.Output interpreterOutput = new Interpreter().interpret(
                "var o = 10 * (1 + 10) / 2\n" +
                        "print \"o is\"\n" +
                        "out o");
        Assertions.assertEquals("\"o is\"\n55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret("print 55.0");
        Assertions.assertEquals("55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret("var a = 1\n" +
                "var b = 10\n" +
                "out a + b\n" +
                "var sum = 10 * (a + b) / 2\n" +
                "out sum + 0");
        Assertions.assertEquals("11.0\n55.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret(
                "var foo = 100.0\n" +
                        "out {foo, 200}");

        Assertions.assertEquals(Arrays.toString(getSequence(100, 200)), interpreterOutput.output);
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
    void testIncorrectMap() {
        String code = "out {100, 101 102}";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
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

    @Test
    void testMoreVariables() {
        String code = "var n = 1\n" +
                "var m = 5\n" +
                "var seq = {n, m}\n" +
                "out map(seq, i -> i ^ 2)\n";
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(new double[]{
                1, 4, 9, 16, 25
        }), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testLargeMap() {
        String code = "var n = 0\n" +
                "var m = 100000\n" +
                "var seq = {n, m}\n" +
                "var squares = map(seq, i -> i ^ 2)\n" +
                "out squares\n";
        double[] seq = getSequence(0, 100000);
        for (int i = 0; i < seq.length; i++) {
            seq[i] = seq[i] * seq[i];
        }
        Interpreter.Output interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(Arrays.toString(seq), interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void reduceTests() {
        Interpreter.Output interpreterOutput = new Interpreter().interpret(
                "var n = reduce({1, 5}, 0, i k -> i + k)\n" + // 15
                        "var m = reduce({1, n}, 2, i k -> k + k) + reduce({1, 2}, n, k i -> i + i)\n" + // 10 + 10 + 4 + 4 = 28
                        "out n + m\n"
        );
        Assertions.assertEquals("83.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret(
                "out reduce({0, 19999}, 0, i k -> i + k)\n"
        );
        Assertions.assertEquals("1.9999E8", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret(
                "var seq = {1,5}\n" +
                        "out reduce(seq, 0, i k -> i + k)\n"
        );
        Assertions.assertEquals("15.0", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());

        interpreterOutput = new Interpreter().interpret(
                "var n = 5\n" +
                        "var sequence = map({1, n}, i -> (-1)^i / (2 * i + 1))\n" +
                        "var pi = 4 * reduce(sequence, 0, x y -> x + y)\n" +
                        "print “pi = “\n" +
                        "out pi"
        );

        Assertions.assertFalse(interpreterOutput.output.isEmpty());
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
