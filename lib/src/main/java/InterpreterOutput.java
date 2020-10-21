import java.util.List;

public class InterpreterOutput {

    public final String output;

    public final String errors;

    InterpreterOutput(List<String> output, List<String> errors) {
        this.output = String.join("\n", output);
        this.errors = String.join("\n", errors);
    }
}
