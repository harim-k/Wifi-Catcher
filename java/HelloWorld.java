public class HelloWorld {
    public static void main(String[] args) {
        String str = "This ::is:: a sen:tence.  This is a question, right?  Yes!  I t is.";
        String delims = "[ :\n]+";
        String[] tokens = str.split(delims);

        for(String text : tokens)
            System.out.println(text);
    }
}