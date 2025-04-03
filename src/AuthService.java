import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class AuthService {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String USERS_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, User> userMap = new HashMap<>();

    public AuthService() {
        loadUsersJson();
    }

    public void start() {
        while (true) {
            System.out.println("\n[1] 로그인  [2] 회원가입  [3] 종료");
            System.out.print("선택: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" , "로그인":
                    if (login()) {
                        return; // 로그인 성공 시 start() 루프 종료
                    }
                    break;
                case "2", "회원가입":
                    signUp();
                    break;
                case "3", "종료":
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private boolean login() {
        while (true) {
            System.out.print("ID 입력: ");
            String id = scanner.nextLine().replaceAll("\\s+", "").toLowerCase();

            if (!userMap.containsKey(id)) {
                System.out.println("존재하지 않는 ID입니다. 다시 입력해주세요.");
                continue;
            }

            System.out.print("비밀번호 입력: ");
            String pw = scanner.nextLine().replaceAll("\\s+", "");

            String hashedInput = hashPassword(pw);
            if (!userMap.get(id).password.equals(hashedInput)) {
                System.out.println("비밀번호가 일치하지 않습니다. 다시 시도해주세요.");
                continue;
            }

            System.out.println(userMap.get(id).nickname + "님, 환영합니다!");
            return true; // 로그인 성공
        }
    }

    private void signUp() {
        String id, pw, nickname;

        while (true) {
            System.out.print("ID (소문자 a-z, 4~12자): ");
            id = scanner.nextLine().replaceAll("\\s+", "").toLowerCase();

            if (!id.matches("[a-z]{4,12}")) {
                System.out.println("형식이 올바르지 않습니다. 다시 입력해주세요.");
                continue;
            }

            if (userMap.containsKey(id)) {
                System.out.println("이미 존재하는 ID입니다. 다른 ID를 입력해주세요.");
                continue;
            }

            break;
        }

        while (true) {
            System.out.print("비밀번호 (8~20자, 대/소문자, 숫자, 특수문자 포함): ");
            pw = scanner.nextLine().replaceAll("\\s+", "");

            if (!isValidPassword(pw)) {
                System.out.println("비밀번호 형식이 올바르지 않습니다. 다시 입력해주세요.");
                continue;
            }

            break;
        }

        while (true) {
            System.out.print("닉네임 (영문/숫자/_.-, 4~12자): ");
            nickname = scanner.nextLine().replaceAll("\\s+", "");

            if (!nickname.matches("[a-zA-Z0-9_.\\-]{4,12}")) {
                System.out.println("닉네임 형식이 올바르지 않습니다.");
                continue;
            }

            // 중복 체크를 for문으로 처리 (람다 오류 방지)
            boolean duplicated = false;
            for (User user : userMap.values()) {
                if (user.nickname.equals(nickname)) {
                    duplicated = true;
                    break;
                }
            }

            if (duplicated) {
                System.out.println("이미 사용 중인 닉네임입니다.");
                continue;
            }

            break;
        }

        String hashedPw = hashPassword(pw);
        User newUser = new User(nickname, hashedPw);
        userMap.put(id, newUser);
        saveUsersJson();

        System.out.println("회원가입이 완료되었습니다.");
    }

    private boolean isValidPassword(String pw) {
        return pw.length() >= 8 && pw.length() <= 20 &&
                pw.matches(".*[a-z].*") &&
                pw.matches(".*[A-Z].*") &&
                pw.matches(".*[0-9].*") &&
                pw.matches(".*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/].*");
    }

    private String hashPassword(String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 처리 실패", e);
        }
    }

    private void loadUsersJson() {
        try {
            if (Files.exists(Paths.get(USERS_FILE))) {
                Reader reader = Files.newBufferedReader(Paths.get(USERS_FILE));
                userMap = gson.fromJson(reader, new TypeToken<Map<String, User>>() {}.getType());
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("users.json 로드 실패");
        }
    }

    private void saveUsersJson() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            gson.toJson(userMap, writer);
        } catch (IOException e) {
            System.out.println("users.json 저장 실패");
        }
    }

    static class User {
        String nickname;
        String password;

        public User(String nickname, String password) {
            this.nickname = nickname;
            this.password = password;
        }
    }
}
