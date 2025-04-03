import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SignUp {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();

        System.out.println("[회원가입]");

        // 아이디 입력
        String id;
        while (true) {
            System.out.print("아이디 (5~10자): ");
            id = sc.nextLine().replaceAll("\\s", "");
            if (id.length() < 5 || id.length() > 10) {
                System.out.println("⛔ 아이디 길이가 유효하지 않습니다.");
                continue;
            }
            File file = new File("user_" + id + ".json");
            if (file.exists()) {
                System.out.println("⛔ 이미 존재하는 아이디입니다.");
                continue;
            }
            break;
        }

        // 비밀번호 입력
        String pw;
        while (true) {
            System.out.print("비밀번호 (5~10자, 특수문자 포함): ");
            pw = sc.nextLine().replaceAll("\\s", "");
            if (!isValidPassword(pw)) {
                System.out.println("⛔ 비밀번호 형식이 올바르지 않습니다.");
                continue;
            }
            break;
        }

        // 닉네임 입력
        String nickname;
        while (true) {
            System.out.print("닉네임: ");
            nickname = sc.nextLine().replaceAll("\\s", "");
            if (nickname.isEmpty()) {
                System.out.println("⛔ 닉네임은 1자 이상이어야 합니다.");
                continue;
            }
            break;
        }

        // 이름 입력
        System.out.print("이름: ");
        String name = sc.nextLine().replaceAll("\\s", "");

        // 보안 질문/답변 입력
        String question = "어릴 때 다닌 초등학교 이름은?";
        System.out.println("보안 질문: " + question);
        System.out.print("보안 질문의 답: ");
        String answer = sc.nextLine().replaceAll("\\s", "");

        // 유저 객체 생성
        User user = new User();
        user.id = id;
        user.password = pw;
        user.nickname = nickname;
        user.name = name;
        user.question = question;
        user.answer = answer;

        // 기본 게임 데이터
        user.level = 1;
        user.exp = 0;
        user.hp = 100;
        user.inventory = Arrays.asList("포션");
        user.position = new int[]{0, 0};

        // JSON 저장
        FileWriter writer = new FileWriter("user_" + id + ".json");
        gson.toJson(user, writer);
        writer.close();

        System.out.println("✅ 회원가입 성공! user_" + id + ".json 파일이 생성되었습니다.");
    }

    // 비밀번호 형식 검사
    public static boolean isValidPassword(String pw) {
        return pw.length() >= 5 && pw.length() <= 10 &&
                Pattern.matches(".*[!@#$%^&*()_+=-].*", pw);
    }
}