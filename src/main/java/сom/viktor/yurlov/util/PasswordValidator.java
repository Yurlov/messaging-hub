package сom.viktor.yurlov.util;

public class PasswordValidator {

    private final int minTotalCharacters;
    private final int minUpperCharacters;
    private final int minLoverCharacters;
    private final int minSpecialCharacters;
    private final int minDigits;

    public PasswordValidator(int minTotalCharacters,
                             int minUpperCharacters,
                             int minLoverCharacters,
                             int minSpecialCharacters,
                             int minDigits) {

        this.minTotalCharacters = minTotalCharacters;
        this.minUpperCharacters = minUpperCharacters;
        this.minLoverCharacters = minLoverCharacters;
        this.minSpecialCharacters = minSpecialCharacters;
        this.minDigits = minDigits;
    }

    public String validate(String password) {
        String result = "";
        if (password == null)
            result += "Password must not be null\n";
        if (password.length() < minTotalCharacters)
            result += "Password must have at least " + minTotalCharacters + " characters\n";
        if (password.chars().filter(Character::isUpperCase).count() < minUpperCharacters)
            result += "Password must have at least " + minUpperCharacters + " upper case characters\n";
        if (password.chars().filter(Character::isLowerCase).count() < minLoverCharacters)
            result += "Password must have at least " + minLoverCharacters + " lover case characters\n";
        if (password.chars().filter(c -> !Character.isAlphabetic(c) && !Character.isDigit(c)).count() < minSpecialCharacters)
            result += "Password must have at least " + minSpecialCharacters + " special characters\n";
        if (password.chars().filter(Character::isDigit).count() < minDigits)
            result += "Password must have at least " + minDigits + " digits\n";
        return result;
    }
}
