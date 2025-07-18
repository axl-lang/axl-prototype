package axl.compiler.analysis.common.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class SourceSection extends SourceLocation {

    public int length;

    public SourceSection(SourceLocation location, int length) {
        super(location.offset, location.line, location.column);
        this.length = length;
    }

    @Override
    public SourceSection clone() {
        SourceSection clone = (SourceSection) super.clone();
        clone.length = length;
        return clone;
    }
}
