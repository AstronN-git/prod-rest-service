import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.prodcontest.service.JwtService;

@SpringBootTest(classes = ru.prodcontest.service.JwtService.class)
public class JwtTest {
    @Autowired
    private JwtService jwtService;

    @Test
    public void testJwtGeneration() {
        String username = "Astron_n";
        System.out.println(jwtService.generateAuthToken(username));
    }
}
