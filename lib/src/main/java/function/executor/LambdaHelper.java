package function.executor;

class LambdaHelper {

    // Extract lambda expression from input string
    String extractLambdaFromFunctionString(String functionString, int separatorIndex) {
        if (separatorIndex == -1) {
            return null;
        }

        return functionString.substring(separatorIndex + 1, functionString.length() - 1)
                .replace("- >", "->")
                .trim();
    }

    // Extract lambda expression from input string
    String[] splitLambda(String lambda) {
        String[] lambdaTokens = lambda.split("->");

        for (int i = 0; i < lambdaTokens.length; i++) {
            lambdaTokens[i] = lambdaTokens[i].trim();
        }

        if (lambdaTokens.length != 2) {
            return null;
        }

        return lambdaTokens;
    }
}
