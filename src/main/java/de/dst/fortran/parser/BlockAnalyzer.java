package de.dst.fortran.parser;

import de.dst.fortran.code.*;
import org.w3c.dom.Element;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.04.17
 * Time: 17:21
 */
public class BlockAnalyzer {

    public final Block block;

    public BlockAnalyzer(Element be) {
        block = new Block(be.getAttribute("name"));
        block.type = be.getAttribute("type");
        block.returnType = Type.parse(be.getAttribute("return"));

        Analyzer.childElements(be).forEach(ce -> {
            if("b".equals(ce.getNodeName())) {
                variables(ce, block.arguments::get);
            } else
            if("common".equals(ce.getNodeName())) {
                Common common = block.commons.get(ce.getAttribute("name"));
                variables(ce, common.members::get);
            } else
            if("dim".equals(ce.getNodeName())) {
                Type type = Type.parse(ce.getAttribute("type"));
                Analyzer.childElements(ce).forEach(de -> {
                    if ("var".equals(de.getNodeName())) {
                        block.variables.get(de.getTextContent()).type(type);
                    } else if ("fun".equals(de.getNodeName())) {
                        Variable var = block.variables.get(de.getAttribute("name")).type(type);
                        Analyzer.childElements(de).forEach(dimel -> {
                            String name = dimel.getNodeName();
                            if ("var".equals(name)) {
                                Variable dv = block.variables.get(dimel.getTextContent());
                                var.dim(dv);
                            } else if ("val".equals(name)) {
                                Integer n = Integer.decode(dimel.getTextContent());
                                var.dim(new Constant(n));
                            }
                        });
                    }
                });
            } else
                parseVariables(ce, block.variables::get);
        });
    }

    private void variables(Element e, Consumer<String> define) {
        Analyzer.childElements(e,"var")
                 .map(Element::getTextContent)
                 .forEach(define);
     }

    private void parseVariables(Element e, Function<String, Variable> define) {
        Analyzer.childElements(e).forEach(ce ->{
            if("var".equals(ce.getNodeName())) {
                String name = ce.getTextContent();
                define.apply(name);
            } else if("rhs".equals(ce.getNodeName())) {

                Function<String, Variable> assign = name -> {
                    Variable v = define.apply(name);
                    block.assigned.add(v.name);
                    return v;
                };

                Analyzer.childElements(e,"var").findAny()
                        .ifPresent(ve->{
                            assign.apply(ve.getTextContent());
                        });

                Analyzer.childElements(e,"fun").findAny()
                        .ifPresent(fe->{
                            assign.apply(fe.getAttribute("name"));
                        });

            } else if(!"c".equals(ce.getNodeName())) {
                parseVariables(ce, define);
            }
        });
    }

    public void dump() {

        System.out.format("%s %s\n", block.name, block.type);
                block.variables.values().forEach(System.out::println);

        System.out.println();
    }
}
