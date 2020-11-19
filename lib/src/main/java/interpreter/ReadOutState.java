package interpreter;

import formatter.ExpressionParserResult;
import formatter.Formatter;
import formatter.SequenceParserResult;
import tools.Constants;

import java.util.Arrays;

public class ReadOutState extends State {

    ReadOutState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        interpreter.currentOut.append(token).append(Constants.SPACE);
        return true;
    }

    @Override
    public void completeLine() {
        readOut(interpreter.currentOut.toString().trim());
    }

    // out works like `var n = ` sequence, but instead of saving result to `numbers` or `sequences` it should be added to `output`
    private void readOut(String out) {
        // First try to read arithmetic expression
        ExpressionParserResult formattedExpression = Formatter.formatExpression(out, interpreter.numbersProvider);
        if (formattedExpression.expression != null) {
            printExpression(formattedExpression.expression);
            return;
        }

        // If it not arithmetic expression, try to read  sequence
        SequenceParserResult formattedSequence = Formatter.formatSequence(interpreter.calculator, out.trim(), interpreter.numbersProvider);
        if (formattedSequence.sequence != null) {
            interpreter.output.add(Arrays.toString(formattedSequence.sequence));
            return;
        }

        // if it starts with map, try to compute `map()`
        if (out.startsWith(Constants.MAP)) {
            printMap(out.substring(4));
        } else if (interpreter.sequences.containsKey(out)) { // if it sequence, add connected array to output.
            interpreter.output.add(Arrays.toString(interpreter.sequences.get(out)));
        } else {
            // all the rest handled as error
            interpreter.errors.add("Invalid expression in out");
        }
    }

    // calculate expression and add to output
    private void printExpression(String expression) {
        Double expressionResult = interpreter.calculator.calc(expression, interpreter.numbersProvider);
        if (expressionResult == null) {
            interpreter.errors.add("Cannot calc expression in out");
        } else {
            interpreter.output.add(String.valueOf(expressionResult));
        }
    }

    // read `map()` body char by char, instead of splitting it to tokens.
    // then try to apply it and add results to output.
    private void printMap(String out) {
        interpreter.mapReader.reset();
        char[] chars = out.toCharArray();
        for (char c : chars) {
            interpreter.mapReader.readNextChar(c);
        }

        if (interpreter.mapReader.isCompleted() && interpreter.mapReader.executor.validate(out.trim())) {
            interpreter.output.add(Arrays.toString(interpreter.mapReader.executor.compute()));
        } else {
            interpreter.errors.add("Cannot apply map");
        }
    }
}
