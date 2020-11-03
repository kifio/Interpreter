package calculator;

import formatter.Formatter;
import provider.NumbersProvider;
import tools.Constants;
import tools.Validator;

import java.util.*;

// Calculator based on my Swift implementation of Reverse Polish Notation,
// but with some fixes and negative numbers supporting.
public class Calculator {

    // Calc arithmetic expression.
    // first parameter is expression, second is variables and numbers assigned to these variables.
    public Double calc(String expression, Map<String, String> variables) {
        String[] tokens = Formatter.getStringWithSpaces(expression).split(" +");

        if (!variables.isEmpty()) {
            for (int i = 0; i < tokens.length; i++) {
                if (variables.containsKey(tokens[i])) {
                    tokens[i] = variables.get(tokens[i]);
                }
            }
        }

        List<String> rpnTokens = convert(tokens);

        return rpnTokens != null ? calc(rpnTokens) : null;
    }

    // Calc arithmetic expression.
    // first parameter is expression, second is provider of numbers for variables from expresion.
    public Double calc(String expression, NumbersProvider numbersProvider) {
        String[] tokens = Formatter.getStringWithSpaces(expression.trim()).split(" +");

        for (int i = 0; i < tokens.length; i++) {
            String valueOfVariable = numbersProvider.getNumberByName(tokens[i]);
            if (valueOfVariable != null) {
                tokens[i] = valueOfVariable;
            }
        }

        List<String> rpnTokens = convert(tokens);

        return rpnTokens != null ? calc(rpnTokens) : null;
    }

    private Double calc(List<String> tokens) {
        Stack<Double> numbers = new Stack<>();

        for (String token : tokens) {
            if (Validator.isNumber(token)) {
                numbers.add(Double.parseDouble(token));
            } else {
                if (!calc(numbers, token)) {
                    return null;
                }
            }
        }

        return numbers.size() != 1 ? null : numbers.pop();
    }

    // Convert expression to reverse polish notation for excluding ( )
    private List<String> convert(String[] tokens) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> operators = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();

            if (isUnarySign(token, i, tokens, result)) {
                i++;
                switch (token) {
                    case Constants.PLUS:
                        result.add(tokens[i]);
                        break;
                    case Constants.MINUS:
                        double f = Double.parseDouble(tokens[i]);
                        String number = String.valueOf(-f);
                        result.add(number);
                        break;
                    default:
                        return null;
                }
            } else if (Validator.isNumber(token)) {
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
            } else if (!tokens[i].isEmpty()) {
                return null;
            }
        }

        for (int i = operators.size() - 1; i >= 0; i--) {
            String element = operators.get(i);
            result.add(element);
        }

        return result;
    }

    private boolean isUnarySign(String token, int i, String[] tokens, ArrayList<String> result) {
        boolean isUnary = Validator.isUnarySign(token);
        boolean isInCorrectPlace = result.isEmpty() || i == 0 || Validator.isPreUnarySign(tokens[i - 1]);
        boolean isApplicable = (i < tokens.length - 1) && Validator.isNumber(tokens[i + 1]);
        return isUnary && isInCorrectPlace && isApplicable;
    }

    // Calculate expression in reverse polish notation
    private boolean calc(Stack<Double> operands, String operator) {
        double b, a;

        if (operands.size() >= 2) {
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
                    operands.push((double) Math.pow(a, b));
                    return true;
                default:
                    System.out.println("Unknown operator " + operator);
                    return false;
            }
        } else {
            return false;
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
