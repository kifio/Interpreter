import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class InterpreterTest {

    @Test
    void testSimpleProgram() {
        Interpreter interpreter = new Interpreter();
        int number = 42;
        String code =
                "var count = " + number + "\n" +
                "out count";
        InterpreterOutput interpreterOutput = interpreter.interpret(code);
        Assertions.assertEquals(interpreterOutput.output, String.valueOf(number));
        Assertions.assertTrue(interpreterOutput.errors.isEmpty());
    }
}
