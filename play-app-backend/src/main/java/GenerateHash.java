import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        System.out.println("BCRYPT_HASH=" + new BCryptPasswordEncoder().encode("admin123"));
    }
}
