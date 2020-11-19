package interpreter;

import tools.Constants;

public class UndefinedState extends State {

    public UndefinedState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        if (token.equals(Constants.VARIABLE)) {
            interpreter.currentState = new ReadVariableNameState(interpreter, this);
        } else if (token.equals(Constants.PRINT)) {
            interpreter.currentState = new PrintStringConstantState(interpreter, this);
        } else if (token.equals(Constants.OUT)) {
            interpreter.currentState = new ReadOutState(interpreter, this);
        } /*else if (token.equals(Constants.SPACE)) {
            previousState = Interpreter.State.UNDEFINED;
            currentState = Interpreter.State.UNDEFINED;
        } */else if (token.startsWith(Constants.START_SEQUENCE)) {
            interpreter.currentState = new ReadSequenceState(interpreter, this);
        } else {
            interpreter.errors.add("Unexpected token: " + token);
            return false;
        }

        return true;
    }
}
