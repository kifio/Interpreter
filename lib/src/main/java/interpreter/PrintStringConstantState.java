package interpreter;

import tools.Constants;

public class PrintStringConstantState extends State {

    PrintStringConstantState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        interpreter.currentStringConstant.append(token).append(Constants.SPACE);
        return true;
    }

    @Override
    public void completeLine() {
        if (previousState instanceof UndefinedState) {
            interpreter.output.add(interpreter.currentStringConstant.toString().trim());
            interpreter.currentStringConstant.setLength(0);
        }
    }
}
