package interpreter;

import tools.Constants;

public class DetermineVariableTypeState extends State {

    DetermineVariableTypeState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        if (token.equals(Constants.START_SEQUENCE)) {
            interpreter.currentSequence.append(token);
            interpreter.currentState = new ReadSequenceState(interpreter, this);
            return true;
        } else if (token.equals(Constants.MAP)) {
            interpreter.currentState = new ReadMapState(interpreter, this);
            return true;
        } else {
            interpreter.currentState = new ReadExpressionState(interpreter, this);
            return interpreter.currentState.handleToken(token);
        }
    }
}
