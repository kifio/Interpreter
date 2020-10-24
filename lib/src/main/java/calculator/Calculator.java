package calculator;

import formatter.Formatter;
import tools.Constants;
import tools.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

public class Calculator {

    public double calc(String expression) {
        String rpnExpression = convert(expression);
        String[] tokens = rpnExpression.split(Constants.SPACE);
        Stack<Double> numbers = new Stack<>();

        for (String token : tokens) {
            if (Validator.isNumber(token)) {
                numbers.add(Double.parseDouble(token));
            } else {
                if (!calc(numbers, token)) {
                    return Double.NaN;
                }
            }
        }

        return numbers.pop();
    }

    public String convert(String expression) {

        String[] tokens = Formatter.getStringWithSpaces(expression).split(Constants.SPACE);
        StringBuilder result = new StringBuilder();
        ArrayList<String> operators = new ArrayList<>();

        for (String t : tokens) {
            String token = t.trim();
            if (Validator.isNumber(token)) {
                result.append(token);
                result.append(Constants.SPACE);
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

                    result.append(o);
                    result.append(Constants.SPACE);

                    operators.remove(operators.size() - 1);
                }

                if (openingBracket == null) {
                    return null;
                }
            } else if (Validator.isSign(token)) {
                if (!operators.isEmpty()) {
                    String lastOperator = operators.get(operators.size() - 1);
                    while (needPopOperator(token, lastOperator)) {
                        result.append(lastOperator);
                        result.append(Constants.SPACE);
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
            result.append(element);

            if (i != 0) {
                result.append(Constants.SPACE);
            }
        }

        return result.toString();
    }

    boolean calc(Stack<Double> operands, String operator) {
        double b, a;

        try {
            b = operands.pop();
            a = operands.pop();
        } catch (EmptyStackException e) {
            return false;
        }

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
                operands.push(Math.pow(a, b));
                return true;
            default:
                throw new IllegalArgumentException("Unknown operator " + operator);
        }
    }

    boolean isOpeningBracket(String token) {
        return Constants.OPENING_BRACKET.equals(token);
    }

    boolean isClosingBracket(String token) {
        return Constants.CLOSING_BRACKET.equals(token);
    }

    boolean isOperatorsStackEmpty(Stack<String> operators) {

        if (operators.isEmpty()) {
            return true;
        }

        for (String o : operators) {
            if (!isOpeningBracket(o)) {
                return false;
            }
        }

        return true;
    }

    int getPriority(String o) {
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

    boolean isRightAssociative(String o) {
        return o.equals(Constants.POW);
    }

    boolean isLeftAssociative(String o) {
        return !o.equals(Constants.POW);
    }

    boolean needPopOperator(String token, String lastOperator) {
        return !isOpeningBracket(lastOperator)
                && ((isRightAssociative(token) && getPriority(token) < getPriority(lastOperator))
                || (isLeftAssociative(token) && getPriority(token) <= getPriority(lastOperator)));
    }
}
