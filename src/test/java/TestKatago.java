import games.go.KatagoUtils;

import java.io.IOException;
import java.util.ArrayList;

public class TestKatago {
    public static void main(String[] args) {
        KatagoUtils.INSTANCE.initKatagoSituationAnalysis(
                "D:\\mirai-hello-world\\assets\\katago\\katago.exe",
                "D:\\mirai-hello-world\\assets\\katago\\analysis_example.cfg"
        );
        ArrayList<int[]> list = new ArrayList<>();
        list.add(new int[]{0, 1});
        System.out.println(getTest());
        //KatagoUtils.INSTANCE.analysis(list, "foo", true)
    }
    public static String getTest() {
        try {
            KatagoUtils.process.getOutputStream().write(("{}" + "\n").getBytes());
            KatagoUtils.process.getOutputStream().flush();


        } catch (IOException e) {
            e.printStackTrace();
        }

        String json;

        while(true) {
            try {
                String read = KatagoUtils.reader.readLine();
                if(read != null) {
                    json = read;
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return json;
    }
}
