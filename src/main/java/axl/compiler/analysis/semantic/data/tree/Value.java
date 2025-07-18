package axl.compiler.analysis.semantic.data.tree;

import axl.compiler.linker.type.data.Type;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public abstract class Value extends SemanticNode implements Elt {

    protected Type result;
}
