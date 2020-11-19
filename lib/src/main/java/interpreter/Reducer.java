package interpreter;

import function.FunctionReader;
import function.executor.ReduceExecutor;
import tools.Constants;

public class Reducer {

    private final Interpreter interpreter;
    private final FunctionReader<Double> reduceReader;


    Reducer(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.reduceReader = new FunctionReader<>(
                new ReduceExecutor(interpreter.calculator, interpreter.sequencesProvider, interpreter.numbersProvider)
        );
    }

    String reduceSequences(String line) {
        int startIndex = line.indexOf(Constants.REDUCE);

        // if line doesn't contains reduce - just return it as is.
        if (startIndex != -1) {

            // Trim `reduce` keyword and recursively looking for `reduce()` expression in result substring.
            String trimmedString = line.substring(startIndex + Constants.REDUCE.length());
            String reducedString = reduceSequences(trimmedString);

            // after replacement i will have only one reduce in line.
            line = line.replace(trimmedString, reducedString);

            Double result;
            char[] chars = reducedString.toCharArray();

            // read, validate, compute `reduce()` body.
            // replace it with result of computation.
            for (char c : chars) {
                reduceReader.readNextChar(c);
                if (reduceReader.isCompleted() && reduceReader.validate()) {
                    result = reduceReader.executor.compute();
                    if (result != null) {
                        line = line.replace(Constants.REDUCE + reduceReader.getFunctionExpression(),
                                result.toString());
                    }
                } else {
                    interpreter.errors.addAll(reduceReader.executor.errors);
                }
            }
        }

        reduceReader.reset();
        return line;
    }

    void stop() {
        reduceReader.executor.stop();
        reduceReader.reset();
    }
}
