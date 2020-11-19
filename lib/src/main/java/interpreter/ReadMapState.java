package interpreter;

public class ReadMapState extends State {


    ReadMapState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    @Override
    public boolean handleToken(String token) {
        interpreter.mapReader.readNextToken(token);
        if (interpreter.mapReader.isCompleted()) {
            return applyMap();
        } else {
            return true;
        }
    }

    // validate map, apply expression to all elements and keep result in `sequences`.
    private boolean applyMap() {
        if (interpreter.mapReader.validate()) {
            if (previousState instanceof DetermineVariableTypeState) {
                interpreter.sequences.put(interpreter.currentVariableName, interpreter.mapReader.executor.compute());
            }
            interpreter.mapReader.reset();
            return true;
        } else {
            interpreter.errors.addAll(interpreter.mapReader.executor.errors);
            interpreter.mapReader.reset();
            return false;
        }
    }
}