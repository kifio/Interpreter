package interpreter;

import tools.Validator;

public class ReadVariableNameState extends State {

    ReadVariableNameState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        if (Validator.isNameAvailable(token)) {
            interpreter.currentVariableName = token;
            interpreter.currentState = new ReadAssignSymbolState(interpreter, this);
            return true;
        } else {
            interpreter.errors.add("Valid name for variable expected");
            return false;
        }
    }
}
