package de.dst.fortran.code.generator.kotlin

import de.dst.fortran.code.Analyzer
import de.dst.fortran.code.Constant
import de.dst.fortran.code.Value
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import java.util.function.Predicate

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  16.01.2019 12:02
 * modified by: $Author$
 * modified on: $Date$
 */

operator fun Element.get(name : String) = Analyzer.childElement(this, name)

fun Element.children() : MutableList<Element> = Analyzer.childElements(this)

fun Element?.all(predicate : Predicate<in Element>) = Analyzer.childElements(this, predicate)
fun Element?.all(name : String) = Analyzer.childElements(this, name)

// node.attributes[name]
operator fun NamedNodeMap?.get(name : String) = this?.getNamedItem(name)?.nodeValue ?: ""

val Element.name : String get() = this.attributes["name"]

fun Element.appendText(text : String) = this.appendChild(this.ownerDocument.createTextNode(text))

fun Value.toInt() : Int = if(this is Constant) this.value.toInt() else -1
