package interpreter;

public abstract class State {

    final Interpreter interpreter;
    final State previousState;

    State(Interpreter interpreter, State previousState) {
        this.interpreter = interpreter;
        this.previousState = previousState;
    }

    public abstract boolean handleToken(String token);

    public void completeLine() {

    }

}
