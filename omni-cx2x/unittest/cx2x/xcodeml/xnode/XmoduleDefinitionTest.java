/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import helper.XmlHelper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test features of the XmoduleDefinition class.
 *
 * @author clementval
 */
public class XmoduleDefinitionTest {

  private static final String module1 =
      "<FmoduleDefinition name=\"mymodule\" lineno=\"4\" " +
      "file=\"./src/mymodule.f90\">" +
      "</FmoduleDefinition>";

  @Test
  public void simpleModuleDefinitionTest(){
    Xnode node = XmlHelper.createXnode(module1);
    XmoduleDefinition mod = new XmoduleDefinition(node.getElement());
    assertNotNull(mod);
    assertEquals("mymodule", mod.getName());
    assertEquals(4, mod.getLineNo());
    assertEquals("./src/mymodule.f90", mod.getFile());
    assertNull(mod.getSymbolTable());
    assertNull(mod.getDeclarationTable());
  }
}