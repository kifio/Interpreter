public class Main {

    public static void main(String[] args) {
        System.out.println(new Interpreter().interpret("print \"hello world\"").output);
    }
}