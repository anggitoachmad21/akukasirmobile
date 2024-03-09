package id.latenight.creativepos.util;

public class CapitalizeText {

    public static String capitalizeText (String givenString) {
        String Separateur = " ,.-;";
        StringBuffer sb = new StringBuffer();
        boolean ToCap = true;
        for (int i = 0; i < givenString.length(); i++) {
            if (ToCap)
                sb.append(Character.toUpperCase(givenString.charAt(i)));
            else
                sb.append(Character.toLowerCase(givenString.charAt(i)));

            if (Separateur.indexOf(givenString.charAt(i)) >=0)
                ToCap = true;
            else
                ToCap = false;
        }
        return sb.toString().trim();
    }

}
