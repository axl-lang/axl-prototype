package axl.compiler.analysis.semantic.data.tree;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SemanticNode {

    private SemanticNode parent;
}
