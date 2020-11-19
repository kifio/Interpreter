package interpreter;

import tools.Constants;

public class ReadAssignSymbolState extends State {

    ReadAssignSymbolState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        if (token.equals(Constants.ASSIGN)) {
            interpreter.currentState = new DetermineVariableTypeState(interpreter, this);
            return true;
        } else {
            interpreter.errors.add("Sequence or number should be assigned to variable");
            return false;
        }
    }
}
