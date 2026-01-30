package pt.estga.shared.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgVectorTypeTest {

    private PgVectorType pgVectorType;

    @Mock
    private ResultSet resultSet;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private SharedSessionContractImplementor session;

    @BeforeEach
    void setUp() {
        pgVectorType = new PgVectorType();
    }

    @Test
    void getSqlType_ShouldReturnTypesOther() {
        assertEquals(Types.OTHER, pgVectorType.getSqlType());
    }

    @Test
    void returnedClass_ShouldReturnFloatArrayClass() {
        assertEquals(float[].class, pgVectorType.returnedClass());
    }

    @Test
    void equals_ShouldReturnTrueForEqualArrays() {
        float[] x = {1.0f, 2.0f};
        float[] y = {1.0f, 2.0f};
        assertTrue(pgVectorType.equals(x, y));
    }

    @Test
    void equals_ShouldReturnFalseForDifferentArrays() {
        float[] x = {1.0f, 2.0f};
        float[] y = {1.0f, 3.0f};
        assertFalse(pgVectorType.equals(x, y));
    }

    @Test
    void equals_ShouldHandleNulls() {
        assertTrue(pgVectorType.equals(null, null));
        assertFalse(pgVectorType.equals(new float[]{1.0f}, null));
        assertFalse(pgVectorType.equals(null, new float[]{1.0f}));
    }

    @Test
    void hashCode_ShouldReturnSameHashCodeForEqualArrays() {
        float[] x = {1.0f, 2.0f};
        float[] y = {1.0f, 2.0f};
        assertEquals(pgVectorType.hashCode(x), pgVectorType.hashCode(y));
    }

    @Test
    void hashCode_ShouldHandleNull() {
        assertEquals(0, pgVectorType.hashCode(null));
    }

    @Test
    void nullSafeGet_ShouldReturnNullWhenResultSetReturnsNull() throws SQLException {
        when(resultSet.getObject(1)).thenReturn(null);
        assertNull(pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }

    @Test
    void nullSafeGet_ShouldParsePGobject() throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue("[1.0,2.0,3.0]");
        
        when(resultSet.getObject(1)).thenReturn(pgObject);
        
        float[] result = pgVectorType.nullSafeGet(resultSet, 1, session, null);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result);
    }

    @Test
    void nullSafeGet_ShouldParseString() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("[1.5, 2.5]");
        
        float[] result = pgVectorType.nullSafeGet(resultSet, 1, session, null);
        assertArrayEquals(new float[]{1.5f, 2.5f}, result);
    }

    @Test
    void nullSafeGet_ShouldThrowExceptionForUnknownType() throws SQLException {
        when(resultSet.getObject(1)).thenReturn(123); // Integer is not expected
        
        assertThrows(SQLException.class, () -> pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }

    @Test
    void nullSafeSet_ShouldSetNullWhenValueIsNull() throws SQLException {
        pgVectorType.nullSafeSet(preparedStatement, null, 1, session);
        verify(preparedStatement).setNull(1, Types.OTHER);
    }

    @Test
    void nullSafeSet_ShouldSetPGobjectWhenValueIsNotNull() throws SQLException {
        float[] value = {1.1f, 2.2f};
        pgVectorType.nullSafeSet(preparedStatement, value, 1, session);
        
        verify(preparedStatement).setObject(eq(1), argThat(obj -> {
            PGobject pg = (PGobject) obj;
            return "vector".equals(pg.getType()) && "[1.1,2.2]".equals(pg.getValue());
        }));
    }

    @Test
    void deepCopy_ShouldReturnNewArray() {
        float[] original = {1.0f, 2.0f};
        float[] copy = pgVectorType.deepCopy(original);
        
        assertArrayEquals(original, copy);
        assertNotSame(original, copy);
    }

    @Test
    void deepCopy_ShouldReturnNullForNullInput() {
        assertNull(pgVectorType.deepCopy(null));
    }

    @Test
    void isMutable_ShouldReturnFalse() {
        assertFalse(pgVectorType.isMutable());
    }

    @Test
    void disassemble_ShouldReturnDeepCopy() {
        float[] original = {1.0f, 2.0f};
        float[] disassembled = (float[]) pgVectorType.disassemble(original);
        
        assertArrayEquals(original, disassembled);
        assertNotSame(original, disassembled);
    }

    @Test
    void assemble_ShouldReturnDeepCopy() {
        float[] cached = {1.0f, 2.0f};
        float[] assembled = pgVectorType.assemble(cached, null);
        
        assertArrayEquals(cached, assembled);
        assertNotSame(cached, assembled);
    }

    @Test
    void replace_ShouldReturnOriginal() {
        float[] original = {1.0f, 2.0f};
        float[] target = {3.0f, 4.0f};
        
        assertSame(original, pgVectorType.replace(original, target, null));
    }

    @Test
    void parseVector_ShouldHandleEmptyVector() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("[]");
        assertArrayEquals(new float[0], pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }

    @Test
    void parseVector_ShouldHandleNoBrackets() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("1,2,3");
        assertArrayEquals(new float[]{1f, 2f, 3f}, pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }

    @Test
    void parseVector_ShouldHandleWhitespace() throws SQLException {
        when(resultSet.getObject(1)).thenReturn(" [ 1.0 , 2.0 ] ");
        assertArrayEquals(new float[]{1.0f, 2.0f}, pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }

    @Test
    void parseVector_ShouldFailOnInvalidNumber() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("[1.0,abc]");
        assertThrows(IllegalArgumentException.class,
                () -> pgVectorType.nullSafeGet(resultSet, 1, session, null));
    }
}
