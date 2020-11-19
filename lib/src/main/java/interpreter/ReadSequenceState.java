package interpreter;

import formatter.Formatter;
import formatter.SequenceParserResult;
import tools.Constants;

public class ReadSequenceState extends State {

    ReadSequenceState(Interpreter interpreter, State state) {
        super(interpreter, state);
    }

    // read sequence {n, m} token by token, then try to make array from this sequence.
    // if it's possible (n and m are integers, n < m), save array to `sequences`.
    @Override
    public boolean handleToken(String token) {

        interpreter.currentSequence.append(token);
        if (token.equals(Constants.END_SEQUENCE)) {

            SequenceParserResult sequenceParserResult = Formatter.formatSequence(
                    interpreter.calculator,
                    interpreter.currentSequence.toString().trim(),
                    interpreter.numbersProvider
            );

            boolean isSequenceValid = sequenceParserResult.sequence != null
                    && sequenceParserResult.errors.isEmpty();

            if (isSequenceValid) {
                interpreter.sequences.put(interpreter.currentVariableName, sequenceParserResult.sequence);
            } else {
                interpreter.errors.addAll(sequenceParserResult.errors);
            }

            interpreter.currentVariableName = null;
            return isSequenceValid;
        }
        return true;
    }
}
