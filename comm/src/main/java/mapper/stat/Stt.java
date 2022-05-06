package mapper.stat;

public class Stt {
    private static String value = "123";
    static {
        System.out.println(value);
    }

    public static String setAndGet(String vv){
        String k = value;
        value = vv;
        return k;
    }

    public static String get(){
        String k = value;
        return k;
    }
}
