package Login;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.sql.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

public class ConsultarPersonalTest {

    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws SQLException {
        //mocks
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testValidarUsuarioAdmin_UsuarioYContraseñaValidos() throws SQLException, ClassNotFoundException {
        // Simular que la conexión devuelve un PreparedStatement válido
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simula que el usuario fue encontrado
        
        // Llamar al método que estamos probando
        boolean resultado = ConsultarPersonal.validarUsuarioAdmin("cris18", "1805");

        // Verificar respuesta
        assertTrue(resultado);


    }
    
    @Test
    public void testValidarUsuarioAdmin_UsuarioIncorrecto() throws SQLException, ClassNotFoundException {
        // Simular que la consulta no devuelve resultados
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simula no se encuentra al usuario

        // Llamar al método que estamos probando
        boolean resultado = ConsultarPersonal.validarUsuarioAdmin("usuarioIncorrecto", "1805");

        // Verificar que la respuesta es false 
        assertFalse(resultado);
    }

    @Test
    public void testValidarUsuarioAdmin_ContraseñaIncorrecta() throws SQLException, ClassNotFoundException {
        // Simular que la consulta devuelve un usuario válido pero la contraseña es incorrecta
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Simula que el usuario fue encontrado
        when(mockResultSet.getString("Contraseña")).thenReturn("incorrecta"); // Simula que la contraseña es incorrecta

        // Llamar al método que estamos probando
        boolean resultado = ConsultarPersonal.validarUsuarioAdmin("cris18", "1800");

        // Verificar que la respuesta es false (contraseña incorrecta)
        assertFalse(resultado);
    }


}
