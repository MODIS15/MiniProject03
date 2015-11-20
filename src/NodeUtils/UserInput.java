package NodeUtils;

import java.util.Scanner;

/**
 * Used to ask user to enter something in terminal
 */
public class UserInput {

    public static String askUser(String question){
        System.out.println(question);
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        return answer;
    }
}
