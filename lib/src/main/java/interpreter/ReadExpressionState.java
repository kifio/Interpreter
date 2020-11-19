package interpreter;

import tools.Validator;

public class ReadExpressionState extends State {

    ReadExpressionState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        String str = token;

        if (interpreter.numbers.containsKey(str)) {
            str = interpreter.numbers.get(token);
        }

        if (Validator.isSign(str) || Validator.isNumber(str)) {
            interpreter.currentExpression.append(str);
            return true;
        } else {
            interpreter.errors.add("Invalid symbol in expression");
            return false;
        }
    }

    @Override
    public void completeLine() {
        if (previousState instanceof DetermineVariableTypeState) {
            calcCurrentExpression();
        }
    }

    // calculate expression kept in `currentExpression` field.
    private void calcCurrentExpression() {
        if (interpreter.currentExpression.length() == 0) {
            interpreter.currentVariableName = null;
            return;
        }

        String expr = interpreter.currentExpression.toString();
        interpreter.currentExpression.setLength(0);

        Double result = interpreter.calculator.calc(expr, interpreter.numbersProvider);

        if (result != null) {
            interpreter.numbers.put(interpreter.currentVariableName, String.valueOf(result));
        }

        interpreter.currentVariableName = null;
    }
}
