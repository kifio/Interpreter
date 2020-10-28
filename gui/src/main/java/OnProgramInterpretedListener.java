import interpreter.Interpreter;

public interface OnProgramInterpretedListener {
    void onProgramInterpreted(Interpreter.Output output);
}
