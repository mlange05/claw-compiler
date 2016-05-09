/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.language.helper.accelerator;

import cx2x.translator.language.ClawLanguage;
import cx2x.translator.transformation.openacc.OpenAccContinuation;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.helper.XelementHelper;
import cx2x.xcodeml.language.AnalyzedPragma;
import cx2x.xcodeml.transformation.Transformer;
import cx2x.xcodeml.xelement.XbaseElement;
import cx2x.xcodeml.xelement.XcodeProgram;
import cx2x.xcodeml.xelement.XfunctionDefinition;
import cx2x.xcodeml.xelement.Xpragma;

/**
 * The class AcceleratorHelper contains only static method to help the
 * generation of the accelerator related pragma during code transformations.
 *
 * @author clementval
 */
public class AcceleratorHelper {

  /**
   * Generate corresponding pragmas to surround the code with a parallel
   * accelerated region.
   * @param claw      ClawLanguage object that tells if the parallel clause is
   *                  enable and where the start pragma is located.
   * @param xcodeml   Object representation of the current XcodeML
   *                  representation in which the pragmas will be generated.
   * @param startStmt Start statement representing the beginning of the parallel
   *                  region.
   * @param endStmt   End statement representing the end of the parallel region.
   * @return Last stmt inserted or null if nothing is inserted.
   */
  private static XbaseElement generateParallelClause(
      ClawLanguage claw, XcodeProgram xcodeml, XbaseElement startStmt,
      XbaseElement endStmt)
  {
    if(claw.hasParallelClause()){
      try {
        Xpragma beginParallel =
            XelementHelper.createEmpty(Xpragma.class, xcodeml);
        beginParallel.setValue(
            claw.getAcceleratorGenerator().getStartParellelDirective()
        );
        Xpragma endParallel =
            XelementHelper.createEmpty(Xpragma.class, xcodeml);
        endParallel.setValue(
            claw.getAcceleratorGenerator().getEndParellelDirective()
        );
        XelementHelper.insertBefore(startStmt, beginParallel);
        XelementHelper.insertAfter(endStmt, endParallel);
        return endParallel;
      } catch (IllegalTransformationException ignored) { }
    }
    return null;
  }

  /**
   * Generate corresponding pragmas applied directly after a CLAW pragma.
   * @param claw      ClawLanguage object that tells if the parallel clause is
   *                  enable and where the start pragma is located.
   * @param startStmt Start statement representing the beginning of the parallel
   *                  region.
   * @param xcodeml   Object representation of the current XcodeML
   *                  representation in which the pragmas will be generated.
   * @return Last stmt inserted or null if nothing is inserted.
   */
  private static XbaseElement generateAcceleratorClause(
      ClawLanguage claw, XcodeProgram xcodeml, XbaseElement startStmt)
  {
    if(claw.hasAcceleratorClause())
    {
      try {
        Xpragma acceleratorPragma = XelementHelper.createEmpty(Xpragma.class,
            xcodeml);
        acceleratorPragma.setValue(
            claw.getAcceleratorGenerator().
                getSingleDirective(claw.getAcceleratorClauses())
        );
        XelementHelper.insertBefore(startStmt, acceleratorPragma);
        return acceleratorPragma;
      } catch (IllegalTransformationException ignored) { }
    }
    return null;
  }

  /**
   * Generate all corresponding pragmas to be applied for accelerator.
   * @param claw      ClawLanguage object that tells which accelerator pragmas
   *                  are enabled.
   * @param xcodeml   Object representation of the current XcodeML
   *                  representation in which the pragmas will be generated.
   * @param startStmt Start statement for all pragma generation.
   * @param endStmt   End statement for all pragma generation that need end
   *                  pragma statement.
   * @return Last stmt inserted.
   */
  public static XbaseElement generateAdditionalDirectives(
      ClawLanguage claw, XcodeProgram xcodeml, XbaseElement startStmt,
      XbaseElement endStmt)
  {
    if(claw.getCurrentTarget() == AcceleratorDirective.NONE){
      return null;
    }

    XbaseElement pragma =
        generateAcceleratorClause(claw, xcodeml, startStmt);
    if(pragma != null){
      startStmt = pragma;
    }
    return generateParallelClause(claw, xcodeml, startStmt, endStmt);
  }

  /**
   * Generate all corresponding pragmas to be applied to an accelerated
   * function/subroutine.
   * @param claw    ClawLanguage object that tells which accelerator pragmas
   *                are enabled.
   * @param xcodeml Object representation of the current XcodeML
   *                representation in which the pragmas will be generated.
   * @param fctDef  Function/subroutine in which accelerator directives are
   *                generated.
   * @throws IllegalTransformationException if new element cannot be created.
   */
  public static void generateRoutineDirectives(ClawLanguage claw,
                                               XcodeProgram xcodeml,
                                               XfunctionDefinition fctDef)
      throws IllegalTransformationException
  {
    if(claw.getCurrentTarget() == AcceleratorDirective.NONE){
      return; // Don't do anything if the target is none
    }

    if(claw.hasParallelClause() || claw.hasAcceleratorClause()) {
      Xpragma routine = XelementHelper.createEmpty(Xpragma.class, xcodeml);
      routine.setValue(claw.getAcceleratorGenerator().getRoutineDirective());
      fctDef.getBody().appendAsFirst(routine);
    }
  }

  /**
   * Generate the correct clauses for private variable on accelerator.
   * @param claw    ClawLanguage object that tells which accelerator pragmas
   *                are enabled.
   * @param xcodeml Object representation of the current XcodeML
   *                representation in which the pragmas will be generated.
   * @param stmt    Statement from which we looks for a parallel clause to
   *                append private clauses.
   * @param var     Variable to generate the private clause.
   * TODO what about OpenMP ?
   */
  public static void generatePrivateClause(ClawLanguage claw,
                                           XcodeProgram xcodeml,
                                           Transformer transformer,
                                           XbaseElement stmt,
                                           String var)
  {
    if(claw.getCurrentTarget() == AcceleratorDirective.NONE
        || !claw.hasPrivateClause()){
      return;
    }

    Xpragma parallel =
        XelementHelper.findPreviousPragma(stmt,
            claw.getAcceleratorGenerator().getParallelKeyword());

    if(parallel == null){
      xcodeml.addWarning("No parallel construct found to attach private clause",
          claw.getPragma().getLineNo());
    } else {
      if(parallel.getValue().length() >= 80){
        parallel.append(" " + claw.getAcceleratorGenerator().getPrefix() + " ");
        transformer.addTransformation(new OpenAccContinuation(
            new AnalyzedPragma(parallel)));
      }
      parallel.append(claw.getAcceleratorGenerator().getPrivateClause(var));
    }
  }

  /**
   * Constructs the correct AcceleratorGenerator object regarding the enum
   * value passed.
   * @param accDirective Enum value that define the generator to be created.
   * @return A specific implementation of an AcceleratorGenerator.
   */
  public static AcceleratorGenerator createAcceleratorGenerator(
          AcceleratorDirective accDirective)
  {
    switch (accDirective){
      case OPENACC:
        return new OpenAcc();
      case OPENMP:
        return new OpenMp();
    }
    return null;
  }

}
