import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Login {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();
        int attempts = 0;

        while (true) {
            System.out.println("[로그인]");
            System.out.print("아이디: ");
            String id = sc.nextLine().replaceAll("\\s", "");

            System.out.print("비밀번호: ");
            String pw = sc.nextLine().replaceAll("\\s", "");

            File file = new File("user_" + id + ".json");
            if (!file.exists()) {
                System.out.println("존재하지 않는 아이디입니다.");
            } else {
                FileReader reader = new FileReader(file);
                User user = gson.fromJson(reader, User.class);
                reader.close();

                if (user.password.equals(pw)) {
                    System.out.println(user.nickname + "님 환영합니다!");
                    // 로그인 성공 시 이후 게임 로직으로 넘기기
                    return;
                } else {
                    System.out.println("비밀번호가 일치하지 않습니다.");
                }
            }

            attempts++;
            if (attempts >= 5) {
                System.out.println("5회 이상 로그인 실패. 프로그램을 종료합니다.");
                return;
            }
        }
    }
}
