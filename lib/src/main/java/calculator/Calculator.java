package calculator;

import formatter.Formatter;
import provider.NumbersProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;

public class Calculator {

    public Float calc(String expression, String variable, String value) {
        String[] tokens = Formatter.getStringWithSpaces(expression).split(Constants.SPACE);

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals(variable)) {
                tokens[i] = value;
            }
        }

        List<String> rpnTokens = convert(tokens);

        return calc(rpnTokens);
    }


    public Float calc(String expression, NumbersProvider numbersProvider) {
        String[] tokens = Formatter.getStringWithSpaces(expression).split(Constants.SPACE);

        for (int i = 0; i < tokens.length; i++) {
            String valueOfVariable = numbersProvider.getNumberByName(tokens[i]);
            if (valueOfVariable != null) {
                tokens[i] = valueOfVariable;
            }
        }

        List<String> rpnTokens = convert(tokens);

        return calc(rpnTokens);
    }

    private Float calc(List<String> tokens) {
        Stack<Float> numbers = new Stack<>();

        for (String token : tokens) {
            if (Validator.isNumber(token)) {
                numbers.add(Float.parseFloat(token));
            } else {
                if (!calc(numbers, token)) {
                    return null;
                }
            }
        }

        return numbers.pop();
    }

    private List<String> convert(String[] tokens) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> operators = new ArrayList<>();

        for (String s : tokens) {
            String token = s.trim();
            if (Validator.isNumber(token)) {
                result.add(token);
            } else if (isOpeningBracket(token)) {
                operators.add(token);
            } else if (isClosingBracket(token)) {
                String openingBracket = null;
                ArrayList<String> op = new ArrayList<>(operators);
                Collections.reverse(op);

                for (String o : op) {

                    if (isOpeningBracket(o)) {
                        openingBracket = o;
                        operators.remove(operators.size() - 1);
                        break;
                    }
                    result.add(o);
                    operators.remove(operators.size() - 1);
                }

                if (openingBracket == null) {
                    return null;
                }
            } else if (Validator.isSign(token)) {
                if (!operators.isEmpty()) {
                    String lastOperator = operators.get(operators.size() - 1);
                    while (needPopOperator(token, lastOperator)) {
                        result.add(lastOperator);
                        operators.remove(operators.size() - 1);
                        if (operators.isEmpty()) {
                            break;
                        } else {
                            lastOperator = operators.get(operators.size() - 1);
                        }
                    }
                }
                operators.add(token);
            }
        }

        for (int i = operators.size() - 1; i >= 0; i--) {
            String element = operators.get(i);
            result.add(element);
        }

        return result;
    }

    private boolean calc(Stack<Float> operands, String operator) {
        float b, a;

        if (operands.size() == 1) {
            b = operands.pop();

            switch (operator) {
                case Constants.PLUS:
                    operands.push(b);
                    return true;
                case Constants.MINUS:
                    operands.push(-b);
                    return true;
                default:
                    throw new IllegalArgumentException("Unknown operator " + operator);
            }

        } else {
            b = operands.pop();
            a = operands.pop();
            switch (operator) {
                case Constants.PLUS:
                    operands.push(a + b);
                    return true;
                case Constants.MINUS:
                    operands.push(a - b);
                    return true;
                case Constants.MULTIPLY:
                    operands.push(a * b);
                    return true;
                case Constants.DIVIDE:
                    operands.push(a / b);
                    return true;
                case Constants.POW:
                    operands.push((float) Math.pow(a, b));
                    return true;
                default:
                    throw new IllegalArgumentException("Unknown operator " + operator);
            }
        }
    }

    private boolean isOpeningBracket(String token) {
        return Constants.OPENING_BRACKET.equals(token);
    }

    private boolean isClosingBracket(String token) {
        return Constants.CLOSING_BRACKET.equals(token);
    }

    private int getPriority(String o) {
        switch (o) {
            case Constants.PLUS:
            case Constants.MINUS:
                return 0;
            case Constants.MULTIPLY:
            case Constants.DIVIDE:
                return 1;
            case Constants.POW:
                return 2;
            default:
                return -1;
        }
    }

    private boolean isRightAssociative(String o) {
        return o.equals(Constants.POW);
    }

    private boolean isLeftAssociative(String o) {
        return !o.equals(Constants.POW);
    }

    private boolean needPopOperator(String token, String lastOperator) {
        return !isOpeningBracket(lastOperator)
                && ((isRightAssociative(token) && getPriority(token) < getPriority(lastOperator))
                || (isLeftAssociative(token) && getPriority(token) <= getPriority(lastOperator)));
    }
}
