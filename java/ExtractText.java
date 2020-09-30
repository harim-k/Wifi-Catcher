
public class ExtractText {
    public static void main(String[] args) {
        String text = "asdf1234";
        System.out.println(text);
        String id = get_id(text);
        System.out.println(id);
        String password = get_password(text);
        System.out.println(password);

    }

    public static String get_id(String text) {
        // extract id from google_vision text

        if (text.contains(":")) {
            String[] text_split = text.split(":");
            text = text_split[1];
        }

        if (text.length() >= 2 && isdigit(text) == false) {
            return text;
        }

        return "";
    }

    public static String get_password(String text) {
        // extract password from google_vision text

        if (text.contains(":")) {
            String[] text_split = text.split(":");
            text = text_split[1];
        }

        if (text.length() >= 8) {
            String removed_text = remove_special_characters(text);
            if (isalpha(removed_text) == false && isalnum(removed_text) == true)
                return text;
        }

        return "";
    }

    public static boolean isalpha(String text) {

        for (char c : text.toCharArray())
            if (Character.isLetter(c) == false)
                return false;

        return true;
    }

    public static boolean isdigit(String text) {

        for (char c : text.toCharArray())
            if (Character.isDigit(c) == false)
                return false;

        return true;
    }

    public static boolean isalnum(String text) {

        for (char c : text.toCharArray())
            if (!(Character.isLetter(c) == true || Character.isDigit(c) == true))
                return false;

        return true;
    }

    public static String remove_special_characters(String text) {
        // remove special characters

        // special characters can be included in password
        String special_characters = "~!@#$%&*?";
        for (char special_character : special_characters.toCharArray())
            text = text.replace(Character.toString(special_character), "");

        return text;
    }

}
