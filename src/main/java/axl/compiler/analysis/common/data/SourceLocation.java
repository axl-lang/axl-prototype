package axl.compiler.analysis.common.data;

import lombok.*;

@Data
@AllArgsConstructor
public class SourceLocation implements Cloneable {

    public int offset;

    public int line;

    public int column;

    @Override
    public SourceLocation clone() {
        try {
            SourceLocation clone = (SourceLocation) super.clone();
            clone.offset = offset;
            clone.line = line;
            clone.column = column;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
