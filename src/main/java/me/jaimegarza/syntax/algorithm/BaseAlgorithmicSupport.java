package me.jaimegarza.syntax.algorithm;

import me.jaimegarza.syntax.cli.Environment;
import me.jaimegarza.syntax.generator.RuntimeData;

public abstract class BaseAlgorithmicSupport implements AlgorithmicSupport {
  /**
   * Every algorithm needs to have the environment defined
   */
  protected Environment environment;
  /**
   * Every algorithm needs to have the runtime defined.
   */
  protected RuntimeData runtimeData;
  
  /**
   * Construct a supporting class for the desired algorithm.  Should be called
   * from subclasses
   * 
   * @param environment is the calling environment
   */
  public BaseAlgorithmicSupport(Environment environment) {
    super();
    this.environment = environment;
    this.runtimeData = environment.getRuntimeData();
  }

}
