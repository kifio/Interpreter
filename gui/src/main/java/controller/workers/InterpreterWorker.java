package controller.workers;

import controller.OnProgramInterpretedListener;
import interpreter.Interpreter;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class InterpreterWorker extends SwingWorker<Interpreter.Output, Void> {

    private final String program;

    private final Interpreter interpreter;
    private final OnProgramInterpretedListener listener;

    public InterpreterWorker(
            Interpreter interpreter,
            String program,
            OnProgramInterpretedListener listener
    ) {
        this.interpreter = interpreter;
        this.program = program;
        this.listener = listener;
    }

    @Override
    public Interpreter.Output doInBackground() {
        Interpreter.Output output = interpreter.interpret(program);
        interpreter.stop();
        return output;
    }

    @Override
    protected void done() {
        super.done();
        if (!isCancelled()) {
            try {
                listener.onProgramInterpreted(get());
            } catch (InterruptedException | ExecutionException e) {
                listener.onProgramInterpreted(new Interpreter.Output("", e.getMessage()));
                e.printStackTrace();
            }
        }
    }
}
