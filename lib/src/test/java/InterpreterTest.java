import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class InterpreterTest {

    @Test
    void testSimpleProgram() {
        int number = 42;
        String code =
                "var count = " + number + "\n" +
                "out count";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(interpreterOutput.output, String.valueOf(number));
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithSyntaxError() {
        String code =
                "var count = ," +
                "out count";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithExpression() {
        String expr = "42+21+11.5";
        String code =
                "var expr = " + expr + "\n" +
                "out expr";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        // TODO: Test result of expr, not the expr.
        Assertions.assertEquals("74.5", interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithValidSequence() {
        String seq = "21,42";
        String code =
                "var seq = {" + seq + "}\n" +
                "out seq";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertEquals(seq, interpreterOutput.output);
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithWrongOrderSequence() {
        String seq = "21, 1";
        String code =
                "var seq = {" + seq + "}\n" +
                        "out seq";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }

    @Test
    void testSimpleProgramWithFloatElementSequence() {
        String seq = "{1.2, 21}";
        String code =
                "var seq = " + seq + "\n" +
                        "out seq";
        InterpreterOutput interpreterOutput = new Interpreter().interpret(code);
        Assertions.assertTrue(interpreterOutput.output.isEmpty());
        Assertions.assertFalse(interpreterOutput.errors.isEmpty());
    }
}
