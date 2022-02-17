import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryClickListenerTest {
    
    @Test
    public void testAdd() {
        System.out.println("This test method should be run");
        assertEquals(42, 19 + 23);
        ServerMock server = new ServerMock();
        PlayerMock player = new PlayerMock(server, "TestPlayer");
    }
}
