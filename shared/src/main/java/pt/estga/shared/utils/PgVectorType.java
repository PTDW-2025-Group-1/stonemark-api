package pt.estga.shared.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class PgVectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return x == null ? 0 : Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object val = rs.getObject(position);
        return switch (val) {
            case null -> null;
            case PGobject pGobject -> parseVector(pGobject.getValue());
            case String s -> parseVector(s);
            default -> throw new SQLException("Unexpected type for vector: " + val.getClass().getName());
        };

    }

    @Override
    public void nullSafeSet(PreparedStatement st, float[] value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        PGobject obj = new PGobject();
        obj.setType("vector");
        obj.setValue(toVectorLiteral(value));
        st.setObject(index, obj);
    }

    @Override
    public float[] deepCopy(float[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(float[] value) {
        return deepCopy(value);
    }

    @Override
    public float[] assemble(Serializable cached, Object owner) {
        return deepCopy((float[]) cached);
    }

    @Override
    public float[] replace(float[] original, float[] target, Object owner) {
        return original;
    }

    private float[] parseVector(String value) {
        if (value == null || value.isEmpty()) {
            return new float[0];
        }
        
        // Trim whitespace first to handle cases like " [ 1.0, 2.0 ] "
        String content = value.trim();
        
        // Remove brackets [ ] if present
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
        }
        
        if (content.trim().isEmpty()) {
            return new float[0];
        }
        
        String[] parts = content.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                vector[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid vector format: " + value, e);
            }
        }
        return vector;
    }

    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
