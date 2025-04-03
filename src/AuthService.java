import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

// 인증(로그인 및 회원가입)을 처리하는 서비스 클래스
public class AuthService {
    private static final Scanner scanner = new Scanner(System.in);
    private static final String USERS_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, User> userMap = new HashMap<>(); // ID를 키로 하는 사용자 정보 맵

    public AuthService() {
        loadUsersJson();
    }

    // 메인 루프: 로그인, 회원가입, 종료 중 선택
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

    // 로그인 처리 메소드
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

    // 회원가입 처리 메소드
    private void signUp() {
        String id, passward, nickname;

        // ID 입력 및 검증
        while (true) {
            System.out.print("ID (소문자 a-z, 4~12자): ");
            id = scanner.nextLine().replaceAll("\\s+", "");

            if (!id.matches("[a-z]{4,12}")) {
                System.out.println("ID는 소문자 a-z만 허용되며, 길이는 4~12자여야 합니다.");
                continue;
            }

            if (userMap.containsKey(id)) {
                System.out.println("이미 존재하는 ID입니다. 다른 ID를 입력해주세요.");
                continue;
            }

            break;
        }

        // 비밀번호 입력 및 검증
        while (true) {
            System.out.print("비밀번호 (8~20자, 대/소문자, 숫자, 특수문자 포함): ");
            passward = scanner.nextLine().replaceAll("\\s+", "");

            if (!isValidPassword(passward)) {
                System.out.println("비밀번호 형식이 올바르지 않습니다. 다시 입력해주세요.");
                continue;
            }

            break;
        }

        // 닉네임 입력 및 중복 체크
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

        String hashedPw = hashPassword(passward);
        User newUser = new User(nickname, hashedPw);
        userMap.put(id, newUser);
        saveUsersJson();

        System.out.println("회원가입이 완료되었습니다.");
    }
    // 비밀번호 유효성 검사 메소드
    private boolean isValidPassword(String passward) {
        return passward.length() >= 8 && passward.length() <= 20 &&
                passward.matches(".*[a-z].*") &&
                passward.matches(".*[A-Z].*") &&
                passward.matches(".*[0-9].*") &&
                passward.matches(".*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/].*");
    }

    // SHA-256 해시 처리 메소드
    private String hashPassword(String passward) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(passward.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 처리 실패", e);
        }
    }

    // JSON 파일에서 사용자 정보 로드
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

    // 사용자 정보를 JSON 파일로 저장
    private void saveUsersJson() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            gson.toJson(userMap, writer);
        } catch (IOException e) {
            System.out.println("users.json 저장 실패");
        }
    }

    // 사용자 정보를 저장할 내부 클래스
    static class User {
        String nickname;
        String password;

        public User(String nickname, String password) {
            this.nickname = nickname;
            this.password = password;
        }
    }
}
