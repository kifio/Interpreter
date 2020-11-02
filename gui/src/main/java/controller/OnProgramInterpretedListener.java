package controller;

import interpreter.Interpreter;

public interface OnProgramInterpretedListener {
    void onStartInterpretation();
    void onProgramInterpreted(Interpreter.Output output);
}
